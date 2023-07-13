package com.example.base.controller.bean.dto.compete_demand;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysCompeteDemandUpdateDto {
    @NotNull
    Long id;
    String description;
    Long price;
}
