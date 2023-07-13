package com.example.base.controller.bean.vo;

import com.example.base.bean.entity.SysMessage;
import com.example.base.service.plain.SysResourceService;
import com.example.base.service.user.SysUserService;
import com.example.base.utils.BeanCopyUtil;
import lombok.Data;
import org.springframework.beans.factory.BeanFactory;

import java.time.LocalDateTime;


@Data
public class SysMessageVo {



    Long id;
    Long senderId;
    String senderUrl;
    String senderName;
    Long receiveId;
    String receiverUrl;
    String receiverName;
    String url;
    String title;
    String message;
    Integer messageType;
    Integer messageCondition;
    LocalDateTime createTime;

    public static SysMessageVo toSysMessageVo(BeanFactory beanFactory, SysMessage sysMessage) {
        SysUserService sysUserService = beanFactory.getBean(SysUserService.class);
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        SysMessageVo copy = BeanCopyUtil.copy(sysMessage, SysMessageVo.class);
        SysUserVo sender = sysUserService.queryById(sysMessage.getSenderId());
        SysUserVo receiver = sysUserService.queryById(sysMessage.getReceiverId());
        //设置头像
        copy.setSenderUrl(sysResourceService.download(sender.getAvatar()));
        copy.setReceiverUrl(sysResourceService.download(receiver.getAvatar()));
        //设置昵称
        copy.setSenderName(sender.getNickname());
        copy.setReceiverName(receiver.getNickname());
        //设置附件URL
        copy.setUrl(sysResourceService.download(sysMessage.getAnnexId()));
        return copy;
    }
}
