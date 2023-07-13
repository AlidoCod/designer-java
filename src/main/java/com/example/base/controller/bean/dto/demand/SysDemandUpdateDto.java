package com.example.base.controller.bean.dto.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "不需要更新的字段，就别传")
@Data
public class SysDemandUpdateDto {

    @NotNull
    Long id;
    String title;
    String theme;
    String body;
    String technicalSelection;
    Long budget;
    Long annexId;
}
