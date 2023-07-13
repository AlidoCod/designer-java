package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.pojo.SysBroadcastMessage;
import com.example.base.controller.bean.dto.message.*;
import com.example.base.controller.bean.vo.MessageListVo;
import com.example.base.controller.bean.vo.SysMessageVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.controller.bean.vo.base.Tuple;
import com.example.base.service.user.SysMessageService;
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

@Slf4j
@Tag(name = "用户消息")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/message")
@Controller
public class SysMessageController {

    final SysMessageService messageService;

    @Aggregation(path = "/sent", method = RequestMethod.POST,
            summary = "发送消息; 后端无法得知用户是否在聊天页面，因此若用户在聊天页面，请调用接口手动签收消息")
    public Result sent(@RequestBody @Valid SysMessageDto messageDto) {
        messageService.sent(messageDto);
        return Result.ok();
    }

    @Aggregation(path = "/query/unread-chat-message-nums", method = RequestMethod.GET,
            summary = "查看未读消息数量", description = "key: senderId, value: 未读消息数量")
    public Result queryUnreadNums(@RequestParam("userId") @NotNull Long userId) {
        List<Tuple> list= messageService.queryUnreadChatMessageNums(userId);
        return Result.data(list);
    }

    @Aggregation(path = "/query/chat-message-nums", method = RequestMethod.GET,
        summary = "查询未读消息"
    )
    public Result chatMessageNums(@RequestParam("senderId") @NotNull Long senderId, @RequestParam("receiverId") Long receiverId) {
        Long nums = messageService.chatMessageNums(senderId, receiverId);
        return Result.data(nums);
    }

    @Aggregation(path = "/query/unread-notice-message-nums", method = RequestMethod.GET,
            summary = "查看未读通知数量")
    public Result queryUnreadNoticeMessageNums() {
        int nums = messageService.queryUnreadNoticeMessageNums();
        return Result.ok(String.valueOf(nums));
    }

    @Aggregation(path = "/query/message-list", method = RequestMethod.GET,
        summary = "外部，查询消息列表"
    )
    public Result queryMessageList(@RequestParam("id") Long id) {
        List<MessageListVo> list = messageService.queryMessageList(id);
        return Result.data(list);
    }

    @Aggregation(path = "/sign/notice-message", method = RequestMethod.POST,
            description = "传参:签收的系统通知ID链表", summary = "签收通知")
    public Result signNoticeMessage(@RequestBody @Valid SignNoticeMessageDto dto) {
        messageService.signNoticeMessage(dto);
        return Result.ok();
    }

    @Aggregation(path = "/sign/chat-message", method = RequestMethod.POST,
            description = "传参：接收的聊天消息ID链表", summary = "签收聊天消息")
    public Result signChatMessage(@RequestBody @Valid SignChatMessageDto dto) {
        messageService.signChatMessage(dto);
        return Result.ok();
    }

    @Aggregation(path = "/query/notice-message", method = RequestMethod.POST,
            description = "建议一次分页拿取数量大于未签收数量", summary = "查询通知")
    public Result queryNoticeMessage(@RequestBody @Valid QueryNoticeMessageDto dto) {
        List<SysMessage> list= messageService.queryNoticeMessage(dto);
        return Result.data(list);
    }

    @Aggregation(path = "/query/chat-message", method = RequestMethod.POST,
            description = "建议一次分页拿取数量大于未签收数量", summary = "查询聊天消息")
    public Result queryChatMessage(@RequestBody QueryChatMessageDto dto) {
        List<SysMessageVo> list = messageService.queryChatMessage(dto);
        return Result.data(list);
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
