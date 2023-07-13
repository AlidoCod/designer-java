package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.dto.search.ContentSearchDto;
import com.example.base.controller.bean.dto.user.SysUserUpdateDto;
import com.example.base.controller.bean.vo.SysUserVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysUserService;
import com.example.base.utils.SecurityContextUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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
    public Result update(@RequestBody @Valid SysUserUpdateDto updateDto) {
        userService.update(SecurityContextUtil.getSysUser().getId(), updateDto);
        return Result.ok();
    }

    @Aggregation(path = "/query", method = RequestMethod.GET, summary = "查询用户信息", description = "无参时获取个人信息, 有参获取他人信息")
    public Result query(@RequestParam(value = "id", required = false) Long id) {
        id = Optional.ofNullable(id).orElse(SecurityContextUtil.getSysUser().getId());
        SysUserVo userVo = userService.queryById(id);
        return Result.data(userVo);
    }

    @Aggregation(path = "/search", method = RequestMethod.POST,
        summary = "搜索用户"
    )
    public Result search(@RequestBody @Valid ContentSearchDto searchDto) {
        List<SysUserVo> list= userService.search(searchDto);
        return Result.data(list);
    }

    @Aggregation(path = "/status", method = RequestMethod.GET,
        summary = "查询用户状态", description = "0: 不在线"
    )
    public Result status(@RequestParam("id") @NotNull Long id) {
        Integer status = userService.status(id);
        return Result.data(status);
    }

}
