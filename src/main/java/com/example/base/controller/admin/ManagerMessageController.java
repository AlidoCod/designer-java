package com.example.base.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.dto.SysBroadcastMessageDto;
import com.example.base.bean.dto.SysNoticeMessageDto;
import com.example.base.bean.dto.UpdateSysBroadcastMessageDto;
import com.example.base.bean.dto.base.BasePage;
import com.example.base.bean.pojo.SysBroadcastMessage;
import com.example.base.bean.vo.result.Result;
import com.example.base.service.admin.ManagerSysMessageService;
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
@Tag(name = "管理消息")
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/message")
@Controller
public class ManagerMessageController {

    final ManagerSysMessageService messageService;

    @Aggregation(path = "broadcast-message/publish", method = RequestMethod.POST, summary = "发布广播消息，系统通知所有用户")
    public Result publishBroadcastMessage(@RequestBody SysBroadcastMessageDto messageDto) {
        messageService.publishBroadcastMessage(messageDto);
        return Result.success();
    }

    @Aggregation(path = "broadcast-message/query-all", method = RequestMethod.POST, summary = "查询所有广播消息")
    public Result queryAllBroadcastMessage(@RequestBody BasePage basePage) {
        Page<SysBroadcastMessage> sysBroadcastMessagePage = messageService.queryAllBroadcastMessage(basePage);
        return Result.data(sysBroadcastMessagePage);
    }

    @Aggregation(path = "broadcast-message/update", method = RequestMethod.POST, summary = "修改广播消息")
    public Result updateBroadcastMessage(@RequestBody UpdateSysBroadcastMessageDto messageDto) {
        messageService.updateBroadcastMessage(messageDto);
        return Result.success();
    }

    @Aggregation(path = "broadcast-message/delete", method = RequestMethod.GET, summary = "删除广播消息")
    public Result deleteBroadcastMessage(@RequestParam("id")Long id) {
        messageService.deleteBroadcastMessage(id);
        return Result.success();
    }

    @Aggregation(path = "notice-message/publish", method = RequestMethod.POST, summary = "发布通知给特定用户")
    public Result publishNotice(@RequestBody SysNoticeMessageDto noticeMessageDto) {
        messageService.publishSysNoticeMessage(noticeMessageDto);
        return Result.success();
    }
}
