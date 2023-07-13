package com.example.base.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.pojo.SysCollectWorks;
import com.example.base.client.redis.RedisSetClient;
import com.example.base.client.redis.RedisZSetClient;
import com.example.base.constant.RedisSetConstant;
import com.example.base.controller.bean.dto.collect_works.SysCollectWorksIsCollectDto;
import com.example.base.controller.bean.vo.SysWorksVo;
import com.example.base.controller.bean.vo.base.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysCollectWorksService {

    final RedisSetClient redisSetClient;
    final RedisZSetClient zSetClient;

    public Boolean isCollect(SysCollectWorksIsCollectDto isCollectDto) {
        return redisSetClient.isMember(RedisSetConstant.COLLECT_WORKS + isCollectDto.getUserId(), isCollectDto.getWorksId());
    }

    final SysWorksService sysWorksService;

    public List<SysWorksVo> query(Page<SysCollectWorks> page, Long userId) {
        return redisSetClient.getMembers(RedisSetConstant.COLLECT_WORKS + userId, Long.class)
                .stream()
                //分页跳过
                .skip(compute(page))
                //拿取数量
                .limit(page.getSize())
                .map(sysWorksService::queryById
                ).toList();
    }

    private long compute(Page page) {
        return (page.getCurrent() - 1) * page.getSize();
    }

    public void insert(SysCollectWorksIsCollectDto insertDto) {
        redisSetClient.addMember(RedisSetConstant.COLLECT_WORKS + insertDto.getUserId(), insertDto.getWorksId());
        //维护收藏量
        zSetClient.increase(RedisSetConstant.RANK_WORKS, String.valueOf(insertDto.getWorksId()));
    }

    public void delete(SysCollectWorksIsCollectDto deleteDto) {
        redisSetClient.removeMember(RedisSetConstant.COLLECT_WORKS + deleteDto.getUserId(), deleteDto.getWorksId());
        //减少收藏量
        zSetClient.decrease(RedisSetConstant.RANK_WORKS, String.valueOf(deleteDto.getWorksId()));
    }

    public Double count(String worksId) {
        return zSetClient.score(RedisSetConstant.RANK_WORKS, worksId);
    }

    public Page<Tuple> ranks(Page<Tuple> page) {
        long start = compute(page);
        List<Tuple> list = zSetClient.rangeWithScore(RedisSetConstant.RANK_WORKS, (int) start, (int) (start + page.getSize()));
        page.setRecords(list);
        return page;
    }

    public Long rank(String worksId) {
        return zSetClient.rank(RedisSetConstant.COLLECT_WORKS, worksId);
    }
}
