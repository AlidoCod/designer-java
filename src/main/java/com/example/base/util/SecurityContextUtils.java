package com.example.base.util;

import com.example.base.bean.entity.SysUser;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtils {

    public static SysUser getSysUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
