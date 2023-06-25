package com.example.base.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.SignNoticeMessageDto;
import com.example.base.bean.dto.QueryChatMessageDto;
import com.example.base.bean.dto.QueryNoticeMessageDto;
import com.example.base.bean.dto.SignChatMessageDto;
import com.example.base.bean.dto.SysMessageDto;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.enums.MessageCondition;
import com.example.base.bean.entity.enums.MessageType;
import com.example.base.bean.vo.SysMessageVo;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.service.JsonService;
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
import java.util.List;
import java.util.Map;

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
        List<SysMessage> sysMessages = messageRepository.selectByMap(Map.of("receiver_id", dto.getReceiverId(), "message_type", MessageType.SYSTEM_NOTICE));
        page.setRecords(sysMessages);
        return page;
    }

    public Page<SysMessage> queryChatMessage(QueryChatMessageDto dto) {
        Page<SysMessage> page = dto.getPage();
        List<SysMessage> sysMessages = messageRepository.selectByMap(Map.of("sender_id", dto.getSenderId(), "receiver_id", dto.getReceiverId(), "message_type", MessageType.CHAT_MESSAGE));
        page.setRecords(sysMessages);
        return page;
    }
}
