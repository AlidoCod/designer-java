package com.example.base.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.controller.bean.dto.message.SignNoticeMessageDto;
import com.example.base.controller.bean.dto.message.QueryChatMessageDto;
import com.example.base.controller.bean.dto.message.QueryNoticeMessageDto;
import com.example.base.controller.bean.dto.message.SignChatMessageDto;
import com.example.base.controller.bean.dto.message.SysMessageDto;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.enums.MessageCondition;
import com.example.base.bean.entity.enums.MessageType;
import com.example.base.bean.pojo.SysBroadcastMessage;
import com.example.base.controller.bean.vo.SysMessageVo;
import com.example.base.client.redis.RedisBitMapClient;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.service.plain.JsonService;
import com.example.base.util.BeanCopyUtils;
import com.example.base.util.SecurityContextUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysMessageService {

    final SysMessageRepository messageRepository;
    final JsonService jsonService;
    final RedisStringClient redisStringClient;

    public void sent(SysMessageDto messageDto) {
        SysMessage sysMessage = SysMessage.sendNormalMessage(SecurityContextUtils.getSysUser().getId(), messageDto.getReceiverId(), messageDto.getAnnexId(), messageDto.getMessage());
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
        //添加未读消息数
        redisStringClient.increase(RedisConstant.chatKeyFormat(sysMessage.getSenderId(), sysMessage.getReceiverId()));
    }

    public int queryUnreadChatMessageNums(Long sendId) {
        return redisStringClient.get(RedisConstant.chatKeyFormat(sendId, SecurityContextUtils.getSysUser().getId()), Integer.class);
    }

    public int queryUnreadNoticeMessageNums() {
        return redisStringClient.get(RedisConstant.NOTICE_ID + SecurityContextUtils.getSysUser().getId(), Integer.class);
    }

    private int signMessage(List<Long> list) {
        List<SysMessage> sysMessages = list.stream().map(o
                -> {
            SysMessage tmp = new SysMessage();
            tmp.setId(o);
            tmp.setMessageCondition(MessageCondition.READ);
            return tmp;
        }).toList();
        int count = 0;
        for (var obj : sysMessages) {
            //防御式编程，当且仅当数据库中的状态为未读且更新成功时才计数，避免前端乱传值
            if (messageRepository.selectById(obj.getId()).getMessageCondition() == MessageCondition.NOT_READ && messageRepository.updateById(obj) == 1) {
                count++;
            }
        }
        return count;
    }

    /**
     * 注解放在这边，避免内部调用导致两个注解失效
     */
    @Transactional
    @Async
    public void signNoticeMessage(SignNoticeMessageDto dto) {
        int count = signMessage(dto.getList());
        redisStringClient.decrease(RedisConstant.NOTICE_ID + dto.getReceiverId(), (long) count);
    }

    @Transactional
    @Async
    public void signChatMessage(SignChatMessageDto dto) {
        int count = signMessage(dto.getList());
        redisStringClient.decrease(RedisConstant.chatKeyFormat(dto.getSenderId(), dto.getReceiverId()), (long) count);
    }

    public Page<SysMessage> queryNoticeMessage(QueryNoticeMessageDto dto) {
        Page<SysMessage> page = dto.getPage();
        LambdaQueryWrapper<SysMessage> lambda = Wrappers.<SysMessage>lambdaQuery()
                .eq(SysMessage::getReceiverId, dto.getReceiverId())
                .eq(SysMessage::getMessageCondition, MessageType.SYSTEM_NOTICE)
                .orderByDesc(SysMessage::getCreateTime);
        return messageRepository.selectPage(page, lambda);
    }

    public Page<SysMessage> queryChatMessage(QueryChatMessageDto dto) {
        Page<SysMessage> page = dto.getPage();
        LambdaQueryWrapper<SysMessage> lambda = Wrappers.<SysMessage>lambdaQuery()
                .eq(SysMessage::getSenderId, dto.getSenderId())
                .eq(SysMessage::getReceiverId, dto.getReceiverId())
                .eq(SysMessage::getMessageCondition, MessageType.CHAT_MESSAGE)
                .orderByDesc(SysMessage::getCreateTime);
        return messageRepository.selectPage(page, lambda);
    }

    final RedisBitMapClient bitMapClient;

    public List<SysBroadcastMessage> queryBroadcastMessage(Long userId) {
        Set<String> keys = redisStringClient.keys(RedisConstant.BROADCAST_MESSAGE + "*");
        List<SysBroadcastMessage> list = new ArrayList<>();
        for (var key : keys) {
            String id = key.substring(key.lastIndexOf(':') + 1);
            Boolean flag = bitMapClient.get(RedisConstant.BROADCAST_BITMAP + id, userId);
            if (!flag) {
                list.add(redisStringClient.get(RedisConstant.BROADCAST_MESSAGE + id, SysBroadcastMessage.class));
            }
        }
        return list;
    }


    public void dontRemindBroadcastMessage(Long userId, Long msgId) {
        bitMapClient.set(RedisConstant.BROADCAST_BITMAP + msgId, userId);
    }
}
