package com.example.base.controller.bean.vo;

import com.example.base.bean.entity.SysWorks;
import com.example.base.service.plain.SysResourceService;
import com.example.base.utils.BeanCopyUtil;
import com.example.base.utils.JsonUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.BeanFactory;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysWorksVo extends SysWorks {

    List<String> annexUrls;

    public static SysWorksVo getInstance(BeanFactory beanFactory, SysWorks sysWorks) {
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        SysWorksVo copy = BeanCopyUtil.copy(sysWorks, SysWorksVo.class);
        //System.out.println(copy);

        //将json强制转化为List, 问题为什么没有触发listHandler?
        List pojo = JsonUtil.toPojo(sysWorks.getAnnexId().toString(), List.class);
        List<String> list = pojo.stream()
                .map(o -> sysResourceService.download(Long.valueOf(o.toString()))).toList();
        copy.setAnnexUrls(list);
        return copy;
    }
}
