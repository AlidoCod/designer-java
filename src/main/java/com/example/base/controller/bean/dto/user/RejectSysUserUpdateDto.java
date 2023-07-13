package com.example.base.controller.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectSysUserUpdateDto {

    @NotNull
    Long id;
    @NotNull
    @Schema(description = "接受消息的用户ID，即cheatId")
    Long userId;
    @NotBlank
    @Schema(description = "消息内容")
    String message;
}
