package com.example.base.controller.bean.dto.collect_works;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysCollectWorksIsCollectDto {

    @NotNull
    Long userId;
    @NotNull
    Long worksId;
}
