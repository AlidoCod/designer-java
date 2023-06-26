package com.example.base.service.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.ExamineDemand;
import com.example.base.bean.entity.SysDemand;
import com.example.base.bean.entity.enums.DemandCondition;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.demand.SysDemandInsertDto;
import com.example.base.controller.bean.dto.demand.SysDemandUpdateDto;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.ExamineDemandRepository;
import com.example.base.repository.SysDemandRepository;
import com.example.base.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysDemandService {

    final SysDemandRepository sysDemandRepository;
    final ExamineDemandRepository examineDemandRepository;

    public void insertDemand(SysDemandInsertDto insertDto) {
        ExamineDemand copy = BeanCopyUtils.copy(insertDto, ExamineDemand.class);
        copy.setId(null);
        copy.setExamineCondition(ExamineCondition.ING);
        examineDemandRepository.insert(copy);
    }

    public void updateDemand(SysDemandUpdateDto updateDto) {
        SysDemand sysDemand = sysDemandRepository.selectById(updateDto.getId());
        if (!sysDemand.getDemandCondition().equals(DemandCondition.IN_BIDDING)){
            throw GlobalRuntimeException.of("需求非竞拍状态，无法更新");
        }
        ExamineDemand copy = BeanCopyUtils.copy(updateDto, ExamineDemand.class);
        copy.setId(null);
        //区分新增和更新
        copy.setCheatId(updateDto.getId());
        copy.setExamineCondition(ExamineCondition.ING);
        examineDemandRepository.insert(copy);
    }

    @Cacheable(value = RedisConstant.CACHE_DEMAND_ID, key = "#root.args[0]")
    public SysDemand queryDemandByDemandId(Long demandId) {
        return sysDemandRepository.selectById(demandId);
    }

    public Page<SysDemand> queryDemandByUserId(Page<SysDemand> page, Long userId) {
        return sysDemandRepository.selectPage(page,
                Wrappers.<SysDemand>lambdaQuery()
                        .eq(SysDemand::getUserId, userId)
        );
    }

    @CacheEvict(value = RedisConstant.CACHE_DEMAND_ID, key = "#root.args[0]")
    public void deleteDemand(Long demandId) {
        SysDemand sysDemand = sysDemandRepository.selectById(demandId);
        if (sysDemand == null) {
            throw GlobalRuntimeException.of("未找到对应的需求，无法删除!");
        }
        if (Objects.equals(sysDemand.getDemandCondition(), DemandCondition.IN_BIDDING)) {
            throw GlobalRuntimeException.of("已合作的需求，无法删除!");
        }
        sysDemandRepository.deleteById(demandId);
    }
}
