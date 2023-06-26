package com.example.base.controller.bean.dto.demand;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysDemandUpdateDto {

    @NotBlank
    Long id;
    String title;
    String theme;
    String body;
    String technicalSelection;
    Long budget;
    Long annexId;
}
