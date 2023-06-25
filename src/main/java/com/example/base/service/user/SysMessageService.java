package com.example.base.service.user;

import com.example.base.bean.dto.SysMessageDto;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.vo.SysMessageVo;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.service.JsonService;
import com.example.base.util.BeanCopyUtils;
import com.example.base.util.SecurityContextUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysMessageService {

    final SysMessageRepository messageRepository;
    final JsonService jsonService;

    public void sent(SysMessageDto messageDto) {
        SysMessage sysMessage = BeanCopyUtils.copy(messageDto, SysMessage.class);
        SysMessage.sendNormalMessage(SecurityContextUtils.getSysUser().getId(), messageDto.getReceiverId(), messageDto.getAnnexId(), messageDto.getMessage());
        //将消息插入数据库
        messageRepository.insert(sysMessage);

        //@TODO 若在线，则发送；否则不发送
        Channel sender = UserConnectPool.getChannel(sysMessage.getSenderId());
        Channel receiver = UserConnectPool.getChannel(sysMessage.getReceiverId());

        //转化对象
        SysMessageVo vo = BeanCopyUtils.copy(sysMessage, SysMessageVo.class);
        //设置创建时间
        vo.setCreateTime(LocalDateTime.now());

        //发送消息
        if (sender != null) {
            sender.writeAndFlush(new TextWebSocketFrame(jsonService.toJson(vo)));
        }
        if (receiver != null) {
            receiver.writeAndFlush(new TextWebSocketFrame(jsonService.toJson(vo)));
        }
    }
}
