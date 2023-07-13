package com.example.base.controller.bean.dto.works;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SysWorksUpdateDto {

    @NotNull
    Long id;
    List<Long> annexId;
    String title;
    String theme;
    String tag;
    String body;
}
