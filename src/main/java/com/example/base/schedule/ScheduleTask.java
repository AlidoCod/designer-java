package com.example.base.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.base.bean.entity.SysCooperateDemand;
import com.example.base.bean.entity.SysDemandSummary;
import com.example.base.bean.entity.SysOrder;
import com.example.base.bean.entity.enums.DemandCondition;
import com.example.base.bean.entity.enums.PaymentCondition;
import com.example.base.bean.entity.enums.WorksCondition;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.repository.SysCooperateDemandRepository;
import com.example.base.repository.SysDemandSummaryRepository;
import com.example.base.repository.SysOrderRepository;
import com.example.base.service.admin.NoticeMessageService;
import com.example.base.service.plain.RabbitService;
import com.example.base.service.user.SysCooperateDemandService;
import com.example.base.service.user.SysDemandService;
import com.example.base.service.user.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScheduleTask {

    final SysDemandSummaryRepository sysDemandSummaryRepository;
    final RabbitService rabbitService;

    /**
     * 设置延时，等待服务器完全初始化降低负载后再开始
     * 延时为一分钟
     */
    @Async
    @Scheduled(fixedRate = 86400000L, initialDelay = 60000L)
    public void updateSysDemandCondition() {
        log.debug("开始清理过期需求");
        //拿到所有竞拍中的需求
        List<SysDemandSummary> list = sysDemandSummaryRepository.selectList(
                Wrappers.<SysDemandSummary>lambdaQuery()
                        .eq(SysDemandSummary::getDemandCondition, DemandCondition.IN_BIDDING)
        );
        //对比是否过期
        LocalDateTime now = LocalDateTime.now();
        for (SysDemandSummary sysDemandSummary : list) {
            //过期
            if (sysDemandSummary.getDeadTime().isBefore(now)) {
                sysDemandSummary.setDemandCondition(DemandCondition.DEAD);
                sysDemandSummaryRepository.updateById(sysDemandSummary);
                log.debug("过期需求ID为: {}", sysDemandSummary.getId());
                //更新到ES
                rabbitService.toMsg(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE, RabbitMQConstant.DEMAND_UPDATE_QUEUE, sysDemandSummary);
            }
        }
        log.debug("清理过期需求完毕");
    }

    final SysCooperateDemandRepository sysCooperateDemandRepository;

    /**
     * 设置延时，延时为两分钟
     */
    @Async
    @Scheduled(fixedRate = 86400000L, initialDelay = 120000L)
    public void updateWorksCondition() {
        log.debug("开始清理过期合作订单");
        LocalDateTime now = LocalDateTime.now();
        sysCooperateDemandRepository.selectList(
                Wrappers.<SysCooperateDemand>lambdaQuery()
                        .select(List.of(SysCooperateDemand::getId, SysCooperateDemand::getDeadTime))
                        .eq(SysCooperateDemand::getWorksCondition, WorksCondition.UNFINISHED)
        ).forEach(o -> {
            //若超时则更新
            if (o.getDeadTime().isBefore(now)) {
                o.setWorksCondition(WorksCondition.TIME_OUT);
                sysCooperateDemandRepository.updateById(o);
            }
        });
        log.debug("清理过期合作订单完毕");
    }

    final NoticeMessageService noticeMessageService;

    final SysUserService sysUserService;

    final SysDemandService sysDemandService;

    final SysCooperateDemandService sysCooperateDemandService;

    final SysOrderRepository sysOrderRepository;
    @Async
    @Scheduled(fixedRate = 1800000L, initialDelay = 30000L)
    public void noticeUserPay() {
        log.debug("开始通知用户支付尾款");
        sysCooperateDemandRepository.selectList(
                Wrappers.<SysCooperateDemand>lambdaQuery()
                        //作品完成且部分支付且存在全款支付订单id，说明此订单存在部分支付和完全支付两种状态
                        .eq(SysCooperateDemand::getWorksCondition, WorksCondition.COMPLETED)
                        .eq(SysCooperateDemand::getPaymentCondition, PaymentCondition.PART_PAID)
                        .isNotNull(SysCooperateDemand::getAllOrder)
        ).forEach(sysCooperateDemand -> {
            SysOrder sysOrder = sysOrderRepository.selectById(sysCooperateDemand.getAllOrder());
            //支付成功的部分，则修改支付状态
            if (sysOrder.getIsPay().equals(PaymentCondition.PAID)) {
                sysCooperateDemand.setPaymentCondition(PaymentCondition.PAID);
                sysCooperateDemandRepository.updateById(sysCooperateDemand);
            }
            //否则催收
            else {
                //通知
                sysCooperateDemandService.noticeUserPay(sysCooperateDemand.getId());
            }
        });
        log.debug("已通知所有未支付尾款的用户");
    }
}
