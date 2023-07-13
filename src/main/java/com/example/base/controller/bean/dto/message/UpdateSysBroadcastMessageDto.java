package com.example.base.controller.bean.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "广播消息请求体")
@Data
public class UpdateSysBroadcastMessageDto {

    @NotNull
    Long id;
    @Schema(description = "消息名称")
    String title;
    @Schema(description = "消息内容，可为html")
    String message;
    @Schema(description = "过期时间, 单位天")
    Long day;
}