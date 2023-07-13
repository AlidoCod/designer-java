package com.example.base.controller.bean.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysNoticeMessageDto {

    @NotNull
    Long receiverId;
    @Schema(description = "通知的标题不能为空")
    @NotBlank
    String title;
    @NotBlank
    String message;
}
