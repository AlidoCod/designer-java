package com.example.base.controller.bean.vo;

import com.example.base.bean.entity.SysDemand;
import com.example.base.service.plain.SysResourceService;
import com.example.base.utils.BeanCopyUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.BeanFactory;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysDemandVo extends SysDemand {
    String annexUrl;

    public static SysDemandVo getInstance(BeanFactory beanFactory, SysDemand sysDemand) {
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        SysDemandVo copy = BeanCopyUtil.copy(sysDemand, SysDemandVo.class);
        copy.setAnnexUrl(sysResourceService.download(copy.getAnnexId()));
        return copy;
    }
}
