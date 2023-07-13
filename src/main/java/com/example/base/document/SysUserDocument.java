package com.example.base.document;

import lombok.Data;

@Data
public class SysUserDocument {

    Long id;
    String username;
    String email;
    String nickname;
    String tag;
}
