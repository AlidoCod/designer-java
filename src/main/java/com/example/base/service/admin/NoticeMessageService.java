package com.example.base.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.enums.MessageCondition;
import com.example.base.bean.entity.enums.MessageType;
import com.example.base.bean.pojo.SysBroadcastMessage;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.message.SysBroadcastMessageDto;
import com.example.base.controller.bean.dto.message.SysNoticeMessageDto;
import com.example.base.controller.bean.dto.message.UpdateSysBroadcastMessageDto;
import com.example.base.controller.bean.vo.SysNoticeMessageVo;
import com.example.base.netty.pojo.MessageAction;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.utils.BeanCopyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeMessageService {

    final RedisStringClient redisStringClient;
    final SysMessageRepository messageRepository;

    public void publishBroadcastMessage(SysBroadcastMessageDto messageDto) {
        SysBroadcastMessage broadcastMessage = BeanCopyUtil.copy(messageDto, SysBroadcastMessage.class);
        broadcastMessage.setId(redisStringClient.increase(RedisConstant.BROADCAST_ID));
        broadcastMessage.setTime(LocalDateTime.now());
        redisStringClient.set(RedisConstant.BROADCAST_MESSAGE + broadcastMessage.getId(), broadcastMessage, messageDto.getDay(), TimeUnit.DAYS);
    }

    public void publishSysNoticeMessage(SysNoticeMessageDto sysNoticeMessageDto) {
        SysMessage sysMessage = BeanCopyUtil.copy(sysNoticeMessageDto, SysMessage.class);
        publishSysNoticeMessage(sysMessage);
    }

    public void publishSysNoticeMessage(SysMessage sysMessage) {
        sysMessage.setMessageType(MessageType.SYSTEM_NOTICE);
        sysMessage.setMessageCondition(MessageCondition.NOT_READ);

        //若在线，则直接发送消息
        SysNoticeMessageVo vo = BeanCopyUtil.copy(sysMessage, SysNoticeMessageVo.class);
        vo.setCreateTime(LocalDateTime.now());
        UserConnectPool.send(sysMessage.getReceiverId(), MessageAction.NOTICE_MESSAGE.ACTION, vo);

        messageRepository.insert(sysMessage);
        //记录未读通知数
        redisStringClient.increase(RedisConstant.NOTICE_ID + sysMessage.getReceiverId());
    }

    public Page<SysBroadcastMessage> queryAllBroadcastMessage(BasePage basePage) {
        Page<SysBroadcastMessage> page = basePage.getPage();
        List<SysBroadcastMessage> list = redisStringClient.gets(RedisConstant.BROADCAST_MESSAGE + "*", SysBroadcastMessage.class);
        page.setRecords(list);
        return page;
    }

    public void updateBroadcastMessage(UpdateSysBroadcastMessageDto messageDto) {

        SysBroadcastMessage broadcastMessage = BeanCopyUtil.copy(messageDto, SysBroadcastMessage.class);
        if (messageDto.getDay() != null) {
            broadcastMessage.setTime(LocalDateTime.now());
            redisStringClient.set(RedisConstant.BROADCAST_MESSAGE + broadcastMessage.getId(), broadcastMessage, messageDto.getDay(), TimeUnit.DAYS);
        }else {
            Long seconds = redisStringClient.getExpire(RedisConstant.BROADCAST_MESSAGE + broadcastMessage.getId());
            redisStringClient.set(RedisConstant.BROADCAST_MESSAGE + broadcastMessage.getId(), broadcastMessage, seconds, TimeUnit.SECONDS);
        }
    }

    public void deleteBroadcastMessage(Long id) {
        redisStringClient.delete(RedisConstant.BROADCAST_MESSAGE + id);
    }
}
