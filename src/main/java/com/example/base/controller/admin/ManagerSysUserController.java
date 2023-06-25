package com.example.base.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.dto.ConfirmSysUserInfoDto;
import com.example.base.bean.dto.base.BasePage;
import com.example.base.bean.vo.result.Result;
import com.example.base.service.admin.ManagerSysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@Tag(name = "管理用户")
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/user-manager")
@Controller
public class ManagerSysUserController {

    final ManagerSysUserService managerSysUserService;

    @Aggregation(path = "/query", method = RequestMethod.POST, summary = "查询所有需要审查的用户信息", description = "草拟的吗，真tm多接口")
    public Result query(@RequestBody BasePage basePage) {
        Page page = managerSysUserService.query(basePage);
        return Result.data(page);
    }

    /**
     * TODO: 2023/6/21 拒绝，消息提醒
     */
    @Aggregation(path = "/confirm", method = RequestMethod.POST, summary = "确认更新", description = "若提供消息字段则为拒绝")
    public Result confirm(@RequestBody ConfirmSysUserInfoDto messageDto) {
        managerSysUserService.confirm(messageDto);
        return Result.success();
    }
}
