package com.example.base.controller.plain;

import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.vo.OrderVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.plain.SysOrderService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Tag(name = "支付接口")
@Validated
@RequiredArgsConstructor
@Controller
public class OrderController {

    final SysOrderService sysOrderService;

    @Aggregation(path = "/pay", method = RequestMethod.GET,
            summary = "支付", description = "生成的支付url"
    )
    public Result pay(@RequestParam("demand-id") Long demandId, @RequestParam("userId") Long userId, @RequestParam("money") String money) {
        OrderVo orderVo = sysOrderService.pay(demandId, userId, money);
        return Result.data(orderVo);
    }

    @Aggregation(path = "/is-pay", method = RequestMethod.GET,
        summary = "查询订单是否支付"
    )
    public Result isPay(@RequestParam("id") Long id) {
        Boolean pay = sysOrderService.isPay(id);
        return Result.data(pay);
    }

    @Aggregation(path = "/id-pay", method = RequestMethod.GET,
        summary = "通过id进行支付"
    )
    public Result payById(@RequestParam("id") Long id) {
        return Result.data(sysOrderService.payById(id));
    }

    @Hidden
    @Aggregation(path = "/pay-success", method = RequestMethod.GET,
        summary = "支付成功路由"
    )
    public Result paySuccess(@RequestParam("id")Long id, @RequestParam("money")String money) {
        String msg = sysOrderService.paySuccess(id, money);
        return Result.ok(msg);
    }
}
