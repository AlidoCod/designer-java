package com.example.base.service.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.ExamineDemand;
import com.example.base.bean.entity.SysDemand;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.enums.DemandCondition;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.constant.GenericConstant;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.demand.RejectDemandDto;
import com.example.base.repository.ExamineDemandRepository;
import com.example.base.repository.SysDemandRepository;
import com.example.base.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManagerSysDemandService {

    final SysDemandRepository sysDemandRepository;
    final ExamineDemandRepository examineDemandRepository;
    final ManagerSysMessageService messageService;

    public Page<ExamineDemand> query(BasePage basePage) {

        Page<ExamineDemand> page = basePage.<ExamineDemand>getPage();
        return examineDemandRepository.selectPage(page,
                Wrappers.<ExamineDemand>lambdaQuery()
                        .eq(ExamineDemand::getExamineCondition, ExamineCondition.ING)
                );
    }

    @Transactional
    public void accept(Long id) {

        ExamineDemand examineDemand = examineDemandRepository.selectById(id);
        SysDemand copy = BeanCopyUtils.copy(examineDemand, SysDemand.class);
        //说明是插入审核
        if (examineDemand.getCheatId() == null) {
            //初始化
            copy.setId(null);
            copy.setDemandCondition(DemandCondition.IN_BIDDING);
            //插入
            sysDemandRepository.insert(copy);
        }//说明是更新审核
        else {
            //重新赋值ID
            copy.setId(examineDemand.getCheatId());
            //更新数据
            sysDemandRepository.updateById(copy);
        }
        //修改审核状态
        ExamineDemand update = new ExamineDemand();
        update.setExamineCondition(ExamineCondition.YES);
        update.setId(examineDemand.getId());
        examineDemandRepository.updateById(update);
    }

    @Transactional
    public void reject(RejectDemandDto rejectDemandDto) {
        //封装消息
        SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(rejectDemandDto.getUserId(), GenericConstant.NOT_EXAMINE, rejectDemandDto.getMessage());
        //发送消息
        messageService.publishSysNoticeMessage(sysMessage);
        //修改审核状态
        ExamineDemand update = new ExamineDemand();
        update.setExamineCondition(ExamineCondition.NO);
        update.setId(rejectDemandDto.getId());
        update.setMessage(rejectDemandDto.getMessage());
        examineDemandRepository.updateById(update);
    }
}
