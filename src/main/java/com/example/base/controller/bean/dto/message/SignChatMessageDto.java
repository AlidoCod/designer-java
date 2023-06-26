package com.example.base.controller.bean.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SignChatMessageDto {

    @Schema(description = "发送人ID")
    Long senderId;
    @Schema(description = "接收人ID")
    Long receiverId;
    @Schema(description = "消息ID")
    List<Long> list;
}
