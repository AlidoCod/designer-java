package com.example.base.controller.bean.vo;

import com.example.base.bean.entity.ExamineUser;
import com.example.base.service.plain.SysResourceService;
import com.example.base.utils.BeanCopyUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.BeanFactory;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExamineUserVo extends ExamineUser {

    String avatarUrl;
    String qualificationUrl;

    public static ExamineUserVo getInstance(BeanFactory beanFactory, ExamineUser examineUser) {
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        ExamineUserVo copy = BeanCopyUtil.copy(examineUser, ExamineUserVo.class);
        copy.setAvatarUrl(sysResourceService.download(examineUser.getAvatar()));
        copy.setQualificationUrl(sysResourceService.download(examineUser.getQualification()));
        return copy;
    }
}
