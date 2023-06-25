package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.bean.dto.SysMessageDto;
import com.example.base.bean.vo.result.Result;
import com.example.base.service.user.SysMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@Tag(name = "用户消息")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/message")
@Controller
public class SysUserMessageController {

    final SysMessageService messageService;

    @Aggregation(path = "/sent", method = RequestMethod.POST, summary = "发送消息")
    public Result sent(@RequestBody SysMessageDto messageDto) {
        messageService.sent(messageDto);
        return Result.success();
    }
}
