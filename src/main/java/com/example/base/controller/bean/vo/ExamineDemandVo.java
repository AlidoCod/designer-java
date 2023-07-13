package com.example.base.controller.bean.vo;

import com.example.base.bean.entity.ExamineDemand;
import com.example.base.service.plain.SysResourceService;
import com.example.base.utils.BeanCopyUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.BeanFactory;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExamineDemandVo extends ExamineDemand {
    String annexUrl;

    public static ExamineDemandVo getInstance(BeanFactory beanFactory, ExamineDemand examineDemand) {
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        String url = sysResourceService.download(examineDemand.getAnnexId());
        ExamineDemandVo copy = BeanCopyUtil.copy(examineDemand, ExamineDemandVo.class);
        copy.setAnnexUrl(url);
        return copy;
    }
}
