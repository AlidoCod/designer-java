package com.example.base.controller.bean.dto.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysDemandRenewalDto {

    @NotNull
    Long id;
    @Schema(description = "前端做出限制，必须比之前的时间晚")
    @Future
    LocalDateTime deadTime;
}
