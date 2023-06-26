package com.example.base.service.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.SysCompeteDemand;
import com.example.base.controller.bean.dto.compete_demand.SysCompeteDemandInsertDto;
import com.example.base.controller.bean.dto.compete_demand.SysCompeteDemandUpdateDto;
import com.example.base.repository.SysCompeteDemandRepository;
import com.example.base.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysCompeteDemandService {

    final SysCompeteDemandRepository sysCompeteDemandRepository;

    /**
     *  若存在则返回true，
     *  不存在则返回false
     */
    public Boolean isExist(Long demandId, Long userId) {
        SysCompeteDemand sysCompeteDemand = sysCompeteDemandRepository.selectOne(
                Wrappers.<SysCompeteDemand>lambdaQuery()
                        .eq(SysCompeteDemand::getDemandId, demandId)
                        .eq(SysCompeteDemand::getCompetitorId, userId)
        );
        return sysCompeteDemand != null;
    }

    public void insert(SysCompeteDemandInsertDto insertDto) {
        SysCompeteDemand copy = BeanCopyUtils.copy(insertDto, SysCompeteDemand.class);
        sysCompeteDemandRepository.insert(copy);
    }

    public Page<SysCompeteDemand> queryByDemandId(Page<SysCompeteDemand> page, Long demandId) {
        return sysCompeteDemandRepository.selectPage(page,
                Wrappers.<SysCompeteDemand>lambdaQuery()
                        .eq(SysCompeteDemand::getDemandId, demandId)
                );
    };

    public Page<SysCompeteDemand> queryByUserId(Page<SysCompeteDemand> page, Long userId) {
        return sysCompeteDemandRepository.selectPage(page,
                Wrappers.<SysCompeteDemand>lambdaQuery()
                        .eq(SysCompeteDemand::getCompetitorId, userId)
                );
    }

    public void update(SysCompeteDemandUpdateDto updateDto) {
        SysCompeteDemand copy = BeanCopyUtils.copy(updateDto, SysCompeteDemand.class);
        sysCompeteDemandRepository.updateById(copy);
    }
}
