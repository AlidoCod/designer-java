package com.example.base.controller.bean.dto.cooperate_demand;

import com.example.base.controller.bean.dto.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysCooperateDemandQueryDto extends BasePage {

    @NotNull
    @Schema(description = "提供用户ID，就查询用户的所有合作订单")
    Long userId;
    @NotNull
    @Schema(description = "提供设计师ID，就查询设计师的所有合作订单")
    Long designerId;
    @Range(min = 0, max = 2)
    @Schema(description = "0: 未完成，1: 已完成, 2:已超时")
    Integer workCondition;
}
