package com.example.base.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.enums.MessageCondition;
import com.example.base.bean.entity.enums.MessageType;
import com.example.base.bean.pojo.SysBroadcastMessage;
import com.example.base.client.redis.RedisBitMapClient;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.message.*;
import com.example.base.controller.bean.vo.MessageListVo;
import com.example.base.controller.bean.vo.SysMessageVo;
import com.example.base.controller.bean.vo.SysUserVo;
import com.example.base.controller.bean.vo.base.Tuple;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.netty.pojo.MessageAction;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.service.plain.SysResourceService;
import com.example.base.utils.BeanCopyUtil;
import com.example.base.utils.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysMessageService {

    final SysMessageRepository sysMessageRepository;
    final RedisStringClient redisStringClient;
    final SysResourceService sysResourceService;
    final BeanFactory beanFactory;
    public void sent(SysMessageDto messageDto) {
        SysMessage sysMessage = SysMessage.sendNormalMessage(SecurityContextUtil.getSysUser().getId(), messageDto.getReceiverId(), messageDto.getAnnexId(), messageDto.getMessage());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //确保消息的可靠性
        sysMessage.setCreateTime(LocalDateTime.parse(messageDto.getCreateTime(), df));
        //将消息插入数据库
        sysMessageRepository.insert(sysMessage);

        //转化对象， 此时获得了插入的id
        SysMessageVo vo = SysMessageVo.toSysMessageVo(beanFactory, sysMessage);

        //向发送者发送消息
        UserConnectPool.send(sysMessage.getSenderId(), MessageAction.CHAT_MESSAGE.ACTION, vo);
        //向接收者发送消息
        UserConnectPool.send(sysMessage.getReceiverId(), MessageAction.CHAT_MESSAGE.ACTION, vo);

        //添加未读消息数
        redisStringClient.increase(RedisConstant.chatKeyFormat(sysMessage.getSenderId(), sysMessage.getReceiverId()));
    }

    public List<Tuple> queryUnreadChatMessageNums(Long userId) {
        Set<String> keys = redisStringClient.keys(RedisConstant.CHAT + "*" + userId);
        List<Tuple> list = new ArrayList<>();
        for (var key : keys) {
            String[] array = key.split("::");
            String senderId = array[1];
            log.debug("senderId: {}", senderId);
            list.add(Tuple.of(senderId,
                    redisStringClient.get(RedisConstant.chatKeyFormat(Long.valueOf(senderId), userId), String.class)));
        }
        return list;
    }

    public int queryUnreadNoticeMessageNums() {
        return redisStringClient.get(RedisConstant.NOTICE_ID + SecurityContextUtil.getSysUser().getId(), Integer.class);
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
            if (sysMessageRepository.selectById(obj.getId()).getMessageCondition() == MessageCondition.NOT_READ && sysMessageRepository.updateById(obj) == 1) {
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

    public List<SysMessage> queryNoticeMessage(QueryNoticeMessageDto dto) {
        Page<SysMessage> page = dto.getPage();
        LambdaQueryWrapper<SysMessage> lambda = Wrappers.<SysMessage>lambdaQuery()
                .eq(SysMessage::getReceiverId, dto.getReceiverId())
                .eq(SysMessage::getMessageType, MessageType.SYSTEM_NOTICE)
                .orderByDesc(SysMessage::getCreateTime);
        return sysMessageRepository.selectPage(page, lambda).getRecords();
    }

    final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    final SysUserService sysUserService;
    public List<SysMessageVo> queryChatMessage(QueryChatMessageDto dto){
        Page<SysMessage> page = dto.getPage();
        LambdaQueryWrapper<SysMessage> lambda1 = Wrappers.<SysMessage>lambdaQuery()
                .eq(SysMessage::getSenderId, dto.getSenderId())
                .eq(SysMessage::getReceiverId, dto.getReceiverId())
                .and( o -> o.eq(SysMessage::getMessageType, MessageType.CHAT_MESSAGE).or()
                        .eq(SysMessage::getMessageType, MessageType.COOPERATE_MESSAGE))
                .orderByAsc(SysMessage::getCreateTime);
        LambdaQueryWrapper<SysMessage> lambda2 = Wrappers.<SysMessage>lambdaQuery()
                .eq(SysMessage::getSenderId, dto.getReceiverId())
                .eq(SysMessage::getReceiverId, dto.getSenderId())
                .and( o -> o.eq(SysMessage::getMessageType, MessageType.CHAT_MESSAGE).or()
                        .eq(SysMessage::getMessageType, MessageType.COOPERATE_MESSAGE))
                .orderByAsc(SysMessage::getCreateTime);

        Future<List<SysMessage>> submit1 = threadPoolTaskExecutor.submit(() -> sysMessageRepository.selectPage(page, lambda1).getRecords());
        Future<List<SysMessage>> submit2 = threadPoolTaskExecutor.submit(() -> sysMessageRepository.selectPage(page, lambda2).getRecords());


        List<SysMessage> list1, list2;
        try {
            //等待接收
            list1 = submit1.get(1000L, TimeUnit.MILLISECONDS);
            list2 = submit2.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("", e);
            throw GlobalRuntimeException.of("Future消息处理出现异常");
        }

        //log.warn(list1.toString());
        //log.warn(list2.toString());


        List<SysMessage> list = new ArrayList<>();
        int i = 0, j = 0;
        //双指针排序O(n)
        while (i < list1.size() || j < list2.size()) {
            //当list1的创建时间早于list2
            while ((i < list1.size()) && ((j >= list2.size()) || list1.get(i).getCreateTime().isBefore(list2.get(j).getCreateTime()))) {
                list.add(list1.get(i));
                i++;
            }
            //当list2的创建时间早于list1
            while ((j < list2.size()) && ((i >= list1.size()) || list2.get(j).getCreateTime().isBefore(list1.get(i).getCreateTime()))) {
                list.add(list2.get(j));
                j++;
            }
        }

        /*
        * 只签收发送给我的消息
        * 不签收发送给他的消息
        * */
        //异步签收消息
        threadPoolTaskExecutor.submit(() -> {
            SysMessage sysMessage = new SysMessage();
            sysMessage.setMessageCondition(MessageCondition.READ);
            int update = sysMessageRepository.update(sysMessage,
                    Wrappers.lambdaUpdate(SysMessage.class)
                            //把所有接收者是自己的消息签收了
                            .eq(SysMessage::getSenderId, dto.getReceiverId())
                            .eq(SysMessage::getReceiverId, dto.getSenderId())
                            .and(o ->
                                    o.eq(SysMessage::getMessageType, MessageType.CHAT_MESSAGE).or()
                                            .eq(SysMessage::getMessageType, MessageType.COOPERATE_MESSAGE)
                            )
                            .eq(SysMessage::getMessageCondition, MessageCondition.NOT_READ)
            );

            redisStringClient.decrease(RedisConstant.chatKeyFormat(dto.getSenderId(), dto.getReceiverId()), (long) update);
            log.debug("senderId: {}, receiverId: {}, 签收了[{}]条未读消息", dto.getSenderId(), dto.getReceiverId(), update);
        });

        SysUserVo sender = sysUserService.queryById(dto.getSenderId());
        SysUserVo receiver = sysUserService.queryById(dto.getReceiverId());

        String senderNickname = sender.getNickname();
        String receiverNickname = receiver.getNickname();

        String senderUrl = sysResourceService.download(sender.getAvatar());
        String receiverUrl = sysResourceService.download(receiver.getAvatar());

        //修改Long id 为URL
        return list.stream().map(o -> {
            SysMessageVo copy = BeanCopyUtil.copy(o, SysMessageVo.class);
            //log.debug("id: {}", o.getAnnexId());
            String url = sysResourceService.download(o.getAnnexId());
            copy.setUrl(url);
            copy.setSenderName(senderNickname);
            copy.setReceiverName(receiverNickname);
            copy.setSenderUrl(senderUrl);
            copy.setReceiverUrl(receiverUrl);
            return copy;
        }).toList();
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

    public List<MessageListVo> queryMessageList(Long id) {
        List<MessageListVo> receiverIdList = sysMessageRepository.selectListBySenderId(id);
        List<MessageListVo> senderIdList = sysMessageRepository.selectListByReceiverId(id);

        //初始化HashMap
        Map<Long, MessageListVo> map = new HashMap<>();
        receiverIdList.forEach(o -> map.put(o.getUserId(), o));

        senderIdList.forEach(o -> {
            //如果包含相同的，就存时间完的
            if (map.containsKey(o.getUserId())) {
                MessageListVo tmp = map.get(o.getUserId());
                if (o.getCreateTime().isAfter(tmp.getCreateTime())) {
                    map.put(o.getUserId(), o);
                }
                //否则不变
            }
            map.put(o.getUserId(), o);
        });

        return map.values().stream().toList();
    }

    public Long chatMessageNums(Long senderId, Long receiverId) {
        return sysMessageRepository.selectCount(
                Wrappers.<SysMessage>lambdaQuery()
                        //把所有接收者是自己的未读消息拿到
                        .eq(SysMessage::getSenderId, receiverId)
                        .eq(SysMessage::getReceiverId, senderId)
                        .and(o -> o.eq(SysMessage::getMessageType, MessageType.CHAT_MESSAGE).or()
                                .eq(SysMessage::getMessageType, MessageType.COOPERATE_MESSAGE))
                        .eq(SysMessage::getMessageCondition, MessageCondition.NOT_READ)
        );
    }
}
