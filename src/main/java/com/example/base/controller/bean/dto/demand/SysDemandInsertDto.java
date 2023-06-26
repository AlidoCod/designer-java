package com.example.base.controller.bean.dto.demand;

import lombok.Data;

@Data
public class SysDemandInsertDto {

    Long userId;
    String title;
    String theme;
    String body;
    String technicalSelection;
    Long budget;
    Long annexId;
}
