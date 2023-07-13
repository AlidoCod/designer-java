package com.example.base.controller.bean.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.checkerframework.checker.optional.qual.Present;

@Data
public class SysMessageDto {

    @NotNull
    Long receiverId;
    @Schema(description = "附件可以不传")
    Long annexId;
    String title;
    String message;
    @Present
    String createTime;
}
