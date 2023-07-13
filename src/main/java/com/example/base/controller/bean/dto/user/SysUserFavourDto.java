package com.example.base.controller.bean.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysUserFavourDto {

    @NotNull
    Long id;
    @NotBlank
    String tag;
}
