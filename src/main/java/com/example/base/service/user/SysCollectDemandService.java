package com.example.base.service.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.SysCollectDemand;
import com.example.base.controller.bean.dto.collect_demand.SysCollectDemandInsertDto;
import com.example.base.repository.SysCollectDemandRepository;
import com.example.base.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysCollectDemandService {

    final SysCollectDemandRepository sysCollectDemandRepository;

    public Boolean isCollect(SysCollectDemandInsertDto isCollectDto) {
        SysCollectDemand sysCollectDemand = sysCollectDemandRepository.selectOne(
                Wrappers.<SysCollectDemand>lambdaQuery()
                        .eq(SysCollectDemand::getDemandId, isCollectDto.getDemandId())
                        .eq(SysCollectDemand::getUserId, isCollectDto.getUserId())
        );
        //!=null表示收藏
        return sysCollectDemand != null;
    }

    public Page<SysCollectDemand> query(Page<SysCollectDemand> page, Long userId) {
        return sysCollectDemandRepository.selectPage(page,
                Wrappers.<SysCollectDemand>lambdaQuery()
                        .eq(SysCollectDemand::getUserId, userId)
                );
    }

    public void insert(SysCollectDemandInsertDto insertDto) {
        SysCollectDemand copy = BeanCopyUtils.copy(insertDto, SysCollectDemand.class);
        sysCollectDemandRepository.insert(copy);
    }

    public void delete(SysCollectDemandInsertDto deleteDto) {
        sysCollectDemandRepository.delete(
                Wrappers.<SysCollectDemand>lambdaQuery()
                        .eq(SysCollectDemand::getDemandId, deleteDto.getDemandId())
                        .eq(SysCollectDemand::getUserId, deleteDto.getUserId())
        );
    }
}
