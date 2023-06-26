package com.example.base.controller.bean.dto.demand;

import com.example.base.controller.bean.dto.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysDemandQueryDto extends BasePage {
    @Schema(description = "若传demandId则不需要填写其他字段；若不传，则需要填写其他字段")
    Long demandId;
    @Schema(description = "查找该用户提出的所有需求")
    Long userId;
}
