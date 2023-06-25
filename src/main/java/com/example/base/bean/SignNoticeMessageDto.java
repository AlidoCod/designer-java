package com.example.base.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SignNoticeMessageDto {

    @Schema(description = "接收人ID")
    Long receiverId;
    @Schema(description = "消息ID")
    List<Long> list;
}
