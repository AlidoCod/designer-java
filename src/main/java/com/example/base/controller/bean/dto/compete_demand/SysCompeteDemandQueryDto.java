package com.example.base.controller.bean.dto.compete_demand;

import com.example.base.controller.bean.dto.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysCompeteDemandQueryDto extends BasePage {

    @Schema(description = "通过用户ID查询参与的竞拍")
    Long userId;

    @Schema(description = "通过需求ID查询此需求下的竞拍")
    Long demandId;
}
