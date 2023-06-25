package com.example.base.bean.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Schema(description = "广播消息请求体")
@Data
public class SysBroadcastMessageDto {

    @JsonIgnore
    public static volatile AtomicInteger id = new AtomicInteger(0);

    @Schema(description = "消息名称")
    String title;
    @Schema(description = "消息内容，可为html")
    String message;
    @Schema(description = "过期时间, 单位天")
    Long day;
}
