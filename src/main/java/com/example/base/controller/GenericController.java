package com.example.base.controller;

import com.example.base.annotation.Aggregation;
import com.example.base.bean.dto.RegisterDto;
import com.example.base.bean.vo.result.Result;
import com.example.base.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Slf4j
@Tag(name = "认证接口")
@Validated
@RequiredArgsConstructor
@Controller
public class AuthenticationController {

    final AuthenticationService authenticationService;

    @Hidden
    @Aggregation(path = "/success", method = RequestMethod.POST, summary = "登录成功返回接口")
    public Result success() {
        String token = authenticationService.authenticate();
        return Result.success(token);
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
        return Result.success(token);
    }

    @Aggregation(path = "/phone-exist", method = RequestMethod.GET,
            summary = "注册前先检查该手机号是否存在", description = "true: 存在，false: 不存在"
    )
    public Result phoneExist(@RequestParam("username")String username) {
        boolean flag = authenticationService.isSysUserExist(username);
        return Result.success(String.valueOf(flag));
    }

    @Aggregation(path = "/verify-code/get", method = RequestMethod.GET,
            summary = "验证码", description = "生成图形验证码，15分钟有效"
    )
    public Result getVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.generateVerifyCode(request, response);
        return Result.success();
    }

    @Aggregation(path = "/verify-code/verify", method = RequestMethod.GET,
            summary = "验证码校验"
    )
    public Result checkVerifyCode(HttpServletRequest request, @RequestParam("code")String code) {
        boolean flag = authenticationService.checkVerifyCode(request, code);
        return Result.success(String.valueOf(flag));
    }
}