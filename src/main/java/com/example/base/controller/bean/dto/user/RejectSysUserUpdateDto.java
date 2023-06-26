package com.example.base.controller.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RejectSysUserUpdateDto {

    Long id;
    @Schema(description = "接受消息的用户ID，即cheatId")
    Long userId;
    @Schema(description = "消息内容")
    String message;
}
