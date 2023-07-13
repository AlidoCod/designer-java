package com.example.base.controller.bean.dto.cooperate_demand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysCooperateDemandInsertDto {

    @NotNull
    @Schema(description = "需求ID")
    Long demandId;
    @NotNull
    @Schema(description = "用户ID")
    Long userId;
    @NotNull
    @Schema(description = "设计师ID")
    Long designerId;
    @NotNull
    String money;
    LocalDateTime deadTime;
    @NotNull
    String tempTime;
}
