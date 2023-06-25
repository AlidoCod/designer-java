package com.example.base.bean.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterDto {

    @Pattern(regexp = "\\d{11}", message = "必须为长度11的数字字符串")
    String username;
    @Length(min = 6, max = 16)
    String password;
}
