package com.example.base.controller.bean.dto.compete_demand;

import lombok.Data;

@Data
public class SysCompeteDemandInsertDto {
    Long competitorId;
    Long demandId;
    String description;
    Long price;
}
