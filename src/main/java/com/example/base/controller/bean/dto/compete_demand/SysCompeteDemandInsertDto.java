package com.example.base.controller.bean.dto.compete_demand;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysCompeteDemandInsertDto {
    @NotNull
    Long competitorId;
    @NotNull
    Long demandId;
    String description;
    @NotNull
    Long price;
}
