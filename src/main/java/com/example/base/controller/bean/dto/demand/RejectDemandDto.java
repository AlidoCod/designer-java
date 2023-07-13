package com.example.base.controller.bean.dto.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectDemandDto {

    @NotNull
    Long id;
    @NotNull
    @Schema(description = "接受审核失败消息的用户Id, 在这里是userId")
    Long userId;
    @NotBlank
    String message;
}
