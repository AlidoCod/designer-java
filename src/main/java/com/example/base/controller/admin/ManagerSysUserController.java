package com.example.base.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.user.RejectSysUserUpdateDto;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.admin.ManagerSysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
    @Aggregation(path = "/accept", method = RequestMethod.GET, summary = "审核通过")
    public Result accept(@RequestParam("id")Long id) {
        managerSysUserService.accept(id);
        return Result.ok();
    }

    @Aggregation(path = "/reject", method = RequestMethod.POST, summary = "审核失败")
    public Result reject(@RequestBody RejectSysUserUpdateDto rejectSysUserUpdateDto) {
        managerSysUserService.reject(rejectSysUserUpdateDto);
        return Result.ok();
    }
}
