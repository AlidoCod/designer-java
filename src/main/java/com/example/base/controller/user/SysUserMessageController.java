package com.example.base.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.dto.message.SignNoticeMessageDto;
import com.example.base.controller.bean.dto.message.QueryChatMessageDto;
import com.example.base.controller.bean.dto.message.QueryNoticeMessageDto;
import com.example.base.controller.bean.dto.message.SignChatMessageDto;
import com.example.base.controller.bean.dto.message.SysMessageDto;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.pojo.SysBroadcastMessage;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Tag(name = "用户消息")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/message")
@Controller
public class SysUserMessageController {

    final SysMessageService messageService;

    @Aggregation(path = "/sent", method = RequestMethod.POST,
            summary = "发送消息")
    public Result sent(@RequestBody SysMessageDto messageDto) {
        messageService.sent(messageDto);
        return Result.ok();
    }

    @Aggregation(path = "/query/unread-chat-message-nums", method = RequestMethod.GET,
            summary = "查看未读消息数量")
    public Result queryUnreadNums(@RequestParam("sendId") Long sendId) {
        int nums = messageService.queryUnreadChatMessageNums(sendId);
        return Result.ok(String.valueOf(nums));
    }

    @Aggregation(path = "/query/unread-notice-message-nums", method = RequestMethod.GET,
            summary = "查看未读通知数量")
    public Result queryUnreadNoticeMessageNums() {
        int nums = messageService.queryUnreadNoticeMessageNums();
        return Result.ok(String.valueOf(nums));
    }

    @Aggregation(path = "/sign/notice-message", method = RequestMethod.POST,
            description = "传参:签收的系统通知ID链表", summary = "签收通知")
    public Result signNoticeMessage(@RequestBody SignNoticeMessageDto dto) {
        messageService.signNoticeMessage(dto);
        return Result.ok();
    }

    @Aggregation(path = "/sign/chat-message", method = RequestMethod.POST,
            description = "传参：接收的聊天消息ID链表", summary = "签收聊天消息")
    public Result signChatMessage(@RequestBody SignChatMessageDto dto) {
        messageService.signChatMessage(dto);
        return Result.ok();
    }

    @Aggregation(path = "/query/notice-message", method = RequestMethod.POST,
            description = "建议一次分页拿取数量大于未签收数量", summary = "查询通知")
    public Result queryNoticeMessage(@RequestBody QueryNoticeMessageDto dto) {
        Page<SysMessage> sysMessagePage = messageService.queryNoticeMessage(dto);
        return Result.data(sysMessagePage);
    }

    @Aggregation(path = "/query/chat-message", method = RequestMethod.POST,
            description = "建议一次分页拿取数量大于未签收数量", summary = "查询聊天消息")
    public Result queryChatMessage(@RequestBody QueryChatMessageDto dto) {
        Page<SysMessage> sysMessagePage = messageService.queryChatMessage(dto);
        return Result.data(sysMessagePage);
    }

    @Aggregation(path = "/query/broadcast-message", method = RequestMethod.GET,
            summary = "抓取广播消息", description = "...")
    public Result queryBroadcastMessage(@RequestParam("user-id") Long userId) {
        List<SysBroadcastMessage> list = messageService.queryBroadcastMessage(userId);
        return Result.data(list);
    }

    @Aggregation(path = "/dont-remind/broadcast-message", method = RequestMethod.GET,
            summary = "不再提醒", description = "")
    public Result dontRemindBroadcastMessage(@RequestParam("user-id")Long userId, @RequestParam("msg-id") Long msgId) {
        messageService.dontRemindBroadcastMessage(userId, msgId);
        return Result.ok();
    }
}
