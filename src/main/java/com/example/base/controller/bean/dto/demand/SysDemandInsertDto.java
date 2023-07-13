package com.example.base.controller.bean.dto.demand;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysDemandInsertDto {

    @NotNull
    Long userId;
    @NotNull
    String title;
    String theme;
    @NotBlank
    String body;
    @NotBlank
    String technicalSelection;
    @NotNull
    Long budget;
    Long annexId;
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime deadTime;
}
