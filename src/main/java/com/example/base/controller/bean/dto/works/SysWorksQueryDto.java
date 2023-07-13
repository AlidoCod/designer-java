package com.example.base.controller.bean.dto.works;

import com.example.base.controller.bean.dto.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysWorksQueryDto extends BasePage {

    @Schema(description = "id,designerId二选一")
    Long id;
    @Schema(description = "id,designerId二选一")
    Long designerId;
}
