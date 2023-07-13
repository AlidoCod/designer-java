package com.example.base.controller.bean.dto.cooperate_demand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysCooperateDemandConfirmDto {

    @NotNull
    Long designerId;
    @NotNull
    Long demandId;
    @NotBlank
    String password;
}
