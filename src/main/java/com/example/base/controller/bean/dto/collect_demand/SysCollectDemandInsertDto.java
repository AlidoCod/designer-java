package com.example.base.controller.bean.dto.collect_demand;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysCollectDemandInsertDto {

    @NotNull
    Long userId;
    @NotNull
    Long demandId;
}
