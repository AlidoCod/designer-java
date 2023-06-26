package com.example.base.controller.bean.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class SysUserUpdateDto {

    @Email
    String email;
    String tag;
    String favour;
    String nickname;
    Long avatar;
    String signature;
}
