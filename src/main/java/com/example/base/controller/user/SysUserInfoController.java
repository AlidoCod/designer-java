package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.bean.dto.SysUserUpdateDto;
import com.example.base.bean.vo.result.Result;
import com.example.base.service.SysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@Tag(name = "用户信息")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/info")
@Controller
public class SysUserInfoDto {

    final SysUserService userService;

    @Aggregation(path = "/update", method = RequestMethod.POST, summary = "更新用户信息", description = "传入需要更新的字段")
    public Result update(SysUserUpdateDto updateDto) {
        String msg = userService.update(updateDto);
        return Result.success(msg);
    }
}
