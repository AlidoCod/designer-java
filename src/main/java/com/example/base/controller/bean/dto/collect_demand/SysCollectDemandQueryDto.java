package com.example.base.controller.bean.dto.collect_demand;

import com.example.base.controller.bean.dto.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysCollectDemandQueryDto extends BasePage {

    Long userId;
}
