package com.example.base.controller.plain;

import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.dto.user.RegisterDto;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.plain.AuthenticationService;
import com.example.base.service.plain.QRCodeService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Tag(name = "通用")
@Validated
@RequiredArgsConstructor
@Controller
public class GenericController {

    final AuthenticationService authenticationService;

    final QRCodeService qrCodeService;

    @Aggregation(path = "/QRCode", method = RequestMethod.GET,
        summary = "生成支付二维码"
    )
    public void generateQRCode(@RequestParam("money") String money, @Parameter(description = "需求和支付订单一对一，直接传需求ID")@RequestParam("id")Long id, HttpServletResponse response) {
        qrCodeService.generatePayQRCode(response, money, id);
    }

    @Hidden
    @Aggregation(path = "/success", method = RequestMethod.POST, summary = "登录成功返回接口")
    public Result success() {
        String token = authenticationService.authenticate();
        return Result.ok(token);
    }

    @Hidden
    @Aggregation(path = "/failure", method = RequestMethod.POST, summary = "登录失败返回接口")
    public Result failure() {
        return Result.fail("登录失败");
    }

    @Aggregation(path = "/register", method = RequestMethod.POST,
            summary = "注册接口"
    )
    public Result register(@RequestBody @Valid RegisterDto registerDto) {
        String token = authenticationService.register(registerDto);
        return Result.ok(token);
    }

    @Aggregation(path = "/phone-exist", method = RequestMethod.GET,
            summary = "注册前先检查该手机号是否存在", description = "true: 存在，false: 不存在"
    )
    public Result phoneExist(@RequestParam("username")String username) {
        boolean flag = authenticationService.isSysUserExist(username);
        return Result.ok(String.valueOf(flag));
    }

    @Aggregation(path = "/verify-code/get", method = RequestMethod.GET,
            summary = "验证码", description = "生成图形验证码，15分钟有效"
    )
    public Result getVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.generateVerifyCode(request, response);
        return Result.ok();
    }

    @Aggregation(path = "/verify-code/verify", method = RequestMethod.GET,
            summary = "验证码校验"
    )
    public Result checkVerifyCode(HttpServletRequest request, @RequestParam("code")String code) {
        boolean flag = authenticationService.checkVerifyCode(request, code);
        return Result.ok(String.valueOf(flag));
    }

    @Hidden
    @Aggregation(path = "/user/test", method = RequestMethod.GET, summary = "测试接口", description = "测试成功", messages = "你好，世界")
    public Result userTest() {
        String s = null;
        Optional<String> optional = Optional.ofNullable(s);
        s = optional.orElse("xxx");
        return Result.data(s);
    }

    @Hidden
    @Aggregation(path = "/admin/test", method = RequestMethod.GET, summary = "测试接口", description = "测试成功", messages = "你好，世界")
    public Result adminTest() {
        String s = null;
        Optional<String> optional = Optional.ofNullable(s);
        s = optional.orElse("xxx");
        return Result.data(s);
    }

    /**
     * 请求转发接口，隐藏就好
     */
    @Hidden
    @GetMapping("/doc")
    public String doc() {
        return "forward:/doc.html";
    }
}
