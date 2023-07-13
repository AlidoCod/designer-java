package com.example.base.service.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.ExamineDemand;
import com.example.base.bean.entity.SysDemand;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.enums.DemandCondition;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.client.redis.RedisZSetClient;
import com.example.base.constant.GenericConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.constant.RedisSetConstant;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.demand.RejectDemandDto;
import com.example.base.controller.bean.vo.ExamineDemandVo;
import com.example.base.controller.bean.vo.SysUserVo;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.ExamineDemandRepository;
import com.example.base.repository.SysDemandRepository;
import com.example.base.service.plain.RabbitService;
import com.example.base.service.user.SysUserService;
import com.example.base.utils.BeanCopyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExamineDemandService {

    final SysDemandRepository sysDemandRepository;
    final ExamineDemandRepository examineDemandRepository;
    final NoticeMessageService noticeMessageService;
    final RedisZSetClient zSetClient;
    final RedisStringClient redisStringClient;
    final BeanFactory beanFactory;
    final SysUserService sysUserService;

    public List<ExamineDemandVo> query(BasePage basePage) {

        Page<ExamineDemand> page = basePage.<ExamineDemand>getPage();
        return examineDemandRepository.selectPage(page,
                        Wrappers.<ExamineDemand>lambdaQuery()
                                .eq(ExamineDemand::getExamineCondition, ExamineCondition.ING)
                ).getRecords().stream()
                .map(examineDemand -> ExamineDemandVo.getInstance(beanFactory, examineDemand))
                .toList();
    }

    final RabbitService rabbitService;

    /**
     * 发送MQ插入ES
     */
    @Transactional
    public void accept(Long id) {

        ExamineDemand examineDemand = examineDemandRepository.selectById(id);
        if (examineDemand == null) {
            throw GlobalRuntimeException.of("审核的ID在数据库并不存在");
        }
        SysDemand copy = BeanCopyUtil.copy(examineDemand, SysDemand.class);
        //说明是插入审核
        if (examineDemand.getCheatId() == null) {
            //初始化
            copy.setId(null);
            copy.setDemandCondition(DemandCondition.IN_BIDDING);
            //插入
            sysDemandRepository.insert(copy);
            //插入数据后，需要设置收藏量
            zSetClient.put(RedisSetConstant.RANK_DEMAND, String.valueOf(copy.getId()));
            //发送MQ
            rabbitService.toMsg(RabbitMQConstant.DESIGNER_INSERT_EXCHANGE, RabbitMQConstant.DEMAND_INSERT_QUEUE, copy);
        }//说明是更新审核
        else {
            //重新赋值ID
            copy.setId(examineDemand.getCheatId());
            //更新数据
            sysDemandRepository.updateById(copy);
            //删除缓存
            redisStringClient.delete(RedisConstant.CACHE_DEMAND_ID + "::" + examineDemand.getCheatId());
            //发送MQ
            rabbitService.toMsg(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE, RabbitMQConstant.DEMAND_UPDATE_QUEUE, copy);
        }
        //修改审核状态
        ExamineDemand update = new ExamineDemand();
        update.setExamineCondition(ExamineCondition.YES);
        update.setId(examineDemand.getId());
        examineDemandRepository.updateById(update);

        //发送通知给用户，告知用户需求更新成功
        String format = "尊敬的【%s】用户，您的标题为: %s的需求，信息更新成功!";
        SysUserVo sysUserVo = sysUserService.queryById(examineDemand.getUserId());
        SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(examineDemand.getUserId(),
                GenericConstant.SYSTEM_NOTICE + "需求信息更新成功",
                String.format(format, sysUserVo.getNickname(), examineDemand.getTitle())
        );
        noticeMessageService.publishSysNoticeMessage(sysMessage);
    }

    @Transactional
    public void reject(RejectDemandDto rejectDemandDto) {
        //封装消息
        SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(rejectDemandDto.getUserId(), GenericConstant.NOT_EXAMINE, rejectDemandDto.getMessage());
        //发送消息
        noticeMessageService.publishSysNoticeMessage(sysMessage);
        //修改审核状态
        ExamineDemand update = new ExamineDemand();
        update.setExamineCondition(ExamineCondition.NO);
        update.setId(rejectDemandDto.getId());
        update.setMessage(rejectDemandDto.getMessage());
        examineDemandRepository.updateById(update);

    }
}
