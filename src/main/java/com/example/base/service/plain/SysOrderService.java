package com.example.base.service.plain;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.base.bean.entity.SysOrder;
import com.example.base.bean.entity.enums.PaymentCondition;
import com.example.base.constant.GenericConstant;
import com.example.base.controller.bean.vo.OrderVo;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.SysOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysOrderService {

    final SysOrderRepository orderRepository;

    public OrderVo pay(Long demandId, Long userId, String money) {

        SysOrder order = SysOrder.builder()
                .relateId(demandId)
                .userId(userId)
                .money(money)
                .isPay(PaymentCondition.PART_PAID).build();
        if (orderRepository.insert(order) == 1) {
            String qrUrl = generatePayUrl(order.getId(), money);
            return OrderVo.builder()
                    .id(order.getId())
                    .url(qrUrl)
                    .build();
        }
        else {
            throw GlobalRuntimeException.of("意料之外的错误，生成订单失败");
        }
    }

    public Boolean isPay(Long id) {
        if (id == null) {
            return false;
        }
        SysOrder sysOrder = orderRepository.selectById(id);
        return sysOrder.getIsPay().equals(PaymentCondition.PAID);
    }

    public String paySuccess(Long id, String money) {
        //设置无期限，避免用户丢失钱
        SysOrder order = SysOrder.builder()
                .isPay(PaymentCondition.PAID)
                .build();
        SysOrder sysOrder = orderRepository.selectById(id);
        if (sysOrder == null) {
            return "订单不存在，支付失败，已退款";
        }
        if (!sysOrder.getMoney().equals(money)) {
            return "支付金额不正确，支付失败，已退款";
        }
        if (sysOrder.getIsPay().equals(PaymentCondition.PAID)) {
            return "订单已支付，请勿重复支付，已退款";
        }
        orderRepository.update(order,
                Wrappers.<SysOrder>lambdaUpdate()
                        .eq(SysOrder::getId, id)
                        .eq(SysOrder::getMoney, money)
                        .eq(SysOrder::getIsPay, PaymentCondition.PART_PAID)
                );
        return String.format(GenericConstant.PAY_SUCCESS_FORMAT, money);
    }


    public String generatePayUrl(Long id, String money) {
        return GenericConstant.ENCRYPTION_PROTOCOL +
                GenericConstant.HOST +
                String.format(GenericConstant.QRCODE_FORMAT, id, money);
    }

    public String payById(Long id) {
        SysOrder sysOrder = orderRepository.selectById(id);
        if (sysOrder != null) {
            return generatePayUrl(sysOrder.getId(), sysOrder.getMoney());
        }
        throw GlobalRuntimeException.of("意料之外的情况，支付ID不存在");
    }
}
