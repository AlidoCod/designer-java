package com.example.base.controller.bean.vo;

import lombok.Data;

@Data
public class SysUserVo {

    Long id;
    String username;
    String email;
    String tag;
    String favour;
    String nickname;
    Long avatar;
    String signature;
}