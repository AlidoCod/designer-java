package com.example.base.controller.bean.dto.compete_demand;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysCompeteDemandIsExistDto {

    @NotNull
    Long userId;
    @NotNull
    Long demandId;
}
