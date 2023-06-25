package com.example.base.bean.pojo;

import com.example.base.bean.entity.enums.Role;
import lombok.Data;

@Data
public class JwtUser {

    Long id;
    String username;
    Role role;
}
