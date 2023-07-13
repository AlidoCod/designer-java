package com.example.base.controller.bean.dto.cooperate_demand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class SysCooperateDemandEvaluationDto {

    @NotNull
    Long id;
    @NotNull
    Long worksId;
    @Schema(description = "最小值: 1, 最大值: 100")
    @Range(min = 1, max = 100)
    Integer grade;
    @Schema(description = "评价不能为空或者空串")
    @NotBlank
    String evaluation;
}
