package com.example.base.utils;

import com.example.base.bean.entity.SysUser;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtil {

    public static SysUser getSysUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
