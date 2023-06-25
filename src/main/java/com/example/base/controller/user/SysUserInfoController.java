package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.bean.dto.SysUserUpdateDto;
import com.example.base.bean.vo.SysUserVo;
import com.example.base.bean.vo.result.Result;
import com.example.base.service.SysUserService;
import com.example.base.util.SecurityContextUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Tag(name = "用户信息")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/info")
@Controller
public class SysUserInfoController {

    final SysUserService userService;

    @Aggregation(path = "/update", method = RequestMethod.POST, summary = "更新用户信息", description = "传入需要更新的字段")
    public Result update(@RequestBody SysUserUpdateDto updateDto) {
        userService.update(SecurityContextUtils.getSysUser().getId(), updateDto);
        return Result.success();
    }

    @Aggregation(path = "/query", method = RequestMethod.GET, summary = "查询用户信息", description = "无参时获取个人信息, 有参获取他人信息")
    public Result query(@RequestParam(value = "id", required = false) Long id) {
        id = Optional.ofNullable(id).orElse(SecurityContextUtils.getSysUser().getId());
        SysUserVo userVo = userService.query(id);
        return Result.data(userVo);
    }
}
