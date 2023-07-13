package com.example.base.controller.bean.vo;

import com.example.base.bean.entity.SysCompeteDemand;
import com.example.base.service.plain.SysResourceService;
import com.example.base.service.user.SysUserService;
import com.example.base.utils.BeanCopyUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class SysCompeteDemandVo extends SysCompeteDemand {

    String nickname;
    String avatarUrl;

    public static SysCompeteDemandVo getInstance(BeanFactory beanFactory, SysCompeteDemand sysCompeteDemand) {
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        SysUserService sysUserService = beanFactory.getBean(SysUserService.class);
        SysUserVo sysUserVo = sysUserService.queryById(sysCompeteDemand.getCompetitorId());
        SysCompeteDemandVo copy = BeanCopyUtil.copy(sysCompeteDemand, SysCompeteDemandVo.class);
        copy.setNickname(sysUserVo.getNickname());
        copy.setAvatarUrl(sysResourceService.download(sysUserVo.getAvatar()));
        return copy;
    }
}
