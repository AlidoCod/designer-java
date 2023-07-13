package com.example.base.controller.bean.dto.works;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SysWorksUploadDto {

    @NotNull
    Long designerId;
    @NotEmpty
    List<Long> annexId;
    @NotBlank
    String title;
    String theme;
    @NotBlank
    String tag;
    @NotBlank
    String body;

}
