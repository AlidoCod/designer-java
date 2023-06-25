package com.example.base.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.dto.SysBroadcastMessageDto;
import com.example.base.bean.dto.SysNoticeMessageDto;
import com.example.base.bean.dto.UpdateSysBroadcastMessageDto;
import com.example.base.bean.dto.base.BasePage;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.enums.MessageCondition;
import com.example.base.bean.entity.enums.MessageType;
import com.example.base.bean.pojo.SysBroadcastMessage;
import com.example.base.bean.vo.SysNoticeMessageVo;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.service.JsonService;
import com.example.base.util.BeanCopyUtils;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManagerSysMessageService {

    final RedisStringClient redisStringClient;
    final SysMessageRepository messageRepository;
    final JsonService jsonService;

    public void publishBroadcastMessage(SysBroadcastMessageDto messageDto) {
        SysBroadcastMessage broadcastMessage = BeanCopyUtils.copy(messageDto, SysBroadcastMessage.class);
        broadcastMessage.setId(SysBroadcastMessage.atomic.getAndAdd(1));
        broadcastMessage.setTime(LocalDateTime.now());
        redisStringClient.set(RedisConstant.BROADCAST_MESSAGE + broadcastMessage.getId(), broadcastMessage, messageDto.getDay(), TimeUnit.DAYS);
    }

    public void publishSysNoticeMessage(SysNoticeMessageDto sysNoticeMessageDto) {
        SysMessage sysMessage = BeanCopyUtils.copy(sysNoticeMessageDto, SysMessage.class);
        sysMessage.setMessageType(MessageType.SYSTEM_NOTICE);
        sysMessage.setMessageCondition(MessageCondition.NOT_READ);
        //若在线，则直接发送消息
        Channel channel = UserConnectPool.getChannel(sysNoticeMessageDto.getReceiverId());
        if (channel != null) {
            SysNoticeMessageVo vo = BeanCopyUtils.copy(sysNoticeMessageDto, SysNoticeMessageVo.class);
            vo.setCreateTime(LocalDateTime.now());
            channel.writeAndFlush(jsonService.toJson(vo));
        }
        messageRepository.insert(sysMessage);
    }

    public void publishSysNoticeMessage(SysMessage sysMessage) {
        sysMessage.setMessageType(MessageType.SYSTEM_NOTICE);
        sysMessage.setMessageCondition(MessageCondition.NOT_READ);
        messageRepository.insert(sysMessage);
    }

    public Page<SysBroadcastMessage> queryAllBroadcastMessage(BasePage basePage) {
        Page<SysBroadcastMessage> page = basePage.getPage();
        List<SysBroadcastMessage> list = redisStringClient.gets(RedisConstant.BROADCAST_MESSAGE + "*", SysBroadcastMessage.class);
        page.setRecords(list);
        return page;
    }

    public void updateBroadcastMessage(UpdateSysBroadcastMessageDto messageDto) {

        SysBroadcastMessage broadcastMessage = BeanCopyUtils.copy(messageDto, SysBroadcastMessage.class);
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
