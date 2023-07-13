package com.example.base.controller.bean.dto.cooperate_demand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "合作完成时，设计师需要上传作品")
@Data
public class SysCooperateDemandWorksUploadDto {

    @NotNull
    @Schema(description = "合作/订单ID")
    Long id;
    @NotNull
    Long demandId;
    @NotNull
    Long designerId;
    @NotEmpty
    List<Long> annexId;
//    @Schema(description = "不能为空")
//    @NotBlank
    String title;
//    @NotBlank
    String body;
}
