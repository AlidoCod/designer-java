package com.example.base.controller.bean.dto.collect_works;

import com.example.base.controller.bean.dto.base.BasePage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysCollectWorksQueryDto extends BasePage {

    @NotNull
    Long userId;
}
