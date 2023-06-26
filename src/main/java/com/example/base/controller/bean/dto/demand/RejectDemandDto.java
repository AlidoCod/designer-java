package com.example.base.controller.bean.dto.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RejectDemandDto {

    Long id;
    @Schema(description = "接受审核失败消息的用户Id, 在这里是userId")
    Long userId;
    String message;
}
