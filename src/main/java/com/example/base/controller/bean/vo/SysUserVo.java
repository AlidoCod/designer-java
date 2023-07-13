package com.example.base.controller.bean.vo;

import com.example.base.bean.entity.SysUser;
import com.example.base.service.plain.SysResourceService;
import com.example.base.utils.BeanCopyUtil;
import lombok.Data;
import org.springframework.beans.factory.BeanFactory;

@Data
public class SysUserVo {

    Long id;
    String username;
    String email;
    String tag;
    String nickname;
    Long avatar;
    String avatarUrl;
    Long qualification;

    public static SysUserVo getInstance(BeanFactory beanFactory, SysUser sysUser) {
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        SysUserVo copy = BeanCopyUtil.copy(sysUser, SysUserVo.class);
        copy.setAvatarUrl(sysResourceService.download(sysUser.getAvatar()));
        return copy;
    }
}
