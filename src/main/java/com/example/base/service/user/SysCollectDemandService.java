package com.example.base.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.pojo.SysCollectDemand;
import com.example.base.client.redis.RedisSetClient;
import com.example.base.client.redis.RedisZSetClient;
import com.example.base.constant.RedisSetConstant;
import com.example.base.controller.bean.dto.collect_demand.SysCollectDemandInsertDto;
import com.example.base.controller.bean.vo.SysDemandVo;
import com.example.base.controller.bean.vo.base.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysCollectDemandService {

    final RedisSetClient redisSetClient;
    final RedisZSetClient zSetClient;

    public Boolean isCollect(SysCollectDemandInsertDto isCollectDto) {
        return redisSetClient.isMember(RedisSetConstant.COLLECT_DEMAND + isCollectDto.getUserId(), isCollectDto.getDemandId());
    }

    final SysDemandService sysDemandService;

    public List<SysDemandVo> query(Page<SysCollectDemand> page, Long userId) {
        return redisSetClient.getMembers(RedisSetConstant.COLLECT_DEMAND + userId, Long.class)
                .stream()
                //分页跳过
                .skip(compute(page))
                //拿取数量
                .limit(page.getSize())
                .map(sysDemandService::queryById
                ).toList();
    }

    private long compute(Page page) {
        return (page.getCurrent() - 1) * page.getSize();
    }

    public void insert(SysCollectDemandInsertDto insertDto) {
        redisSetClient.addMember(RedisSetConstant.COLLECT_DEMAND + insertDto.getUserId(), insertDto.getDemandId());
        //维护收藏量
        zSetClient.increase(RedisSetConstant.RANK_DEMAND, String.valueOf(insertDto.getDemandId()));
    }

    public void delete(SysCollectDemandInsertDto deleteDto) {
        log.debug("{}", redisSetClient.removeMember(RedisSetConstant.COLLECT_DEMAND + deleteDto.getUserId(), deleteDto.getDemandId()));
        //维护收藏量
        zSetClient.decrease(RedisSetConstant.RANK_DEMAND, String.valueOf(deleteDto.getDemandId()));
    }

    public Double count(String demandId) {
        return zSetClient.score(RedisSetConstant.RANK_DEMAND, demandId);
    }

    public Page<Tuple> ranks(Page<Tuple> page) {
        long start = compute(page);
        List<Tuple> list = zSetClient.rangeWithScore(RedisSetConstant.RANK_DEMAND, (int) start, (int) (start + page.getSize()));
        page.setRecords(list);
        return page;
    }

    public Long rank(String demandId) {
        return zSetClient.rank(RedisSetConstant.COLLECT_DEMAND, demandId);
    }
}
