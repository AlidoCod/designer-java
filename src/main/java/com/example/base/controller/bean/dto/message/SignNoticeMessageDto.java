package com.example.base.controller.bean.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class SignNoticeMessageDto {

    @NonNull
    @Schema(description = "接收人ID")
    Long receiverId;
    @NotBlank
    @Schema(description = "消息ID链表")
    List<Long> list;
}
