package com.example.base.controller.bean.dto.cooperate_demand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RenewalDto {

    @NotNull
    Long id;
    @Future
    @Schema(description = "续期时间必须比原时间晚")
    LocalDateTime deadTime;
}
