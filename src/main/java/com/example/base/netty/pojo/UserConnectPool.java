package com.example.base.netty.pojo;

import com.example.base.utils.JsonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class UserConnectPool {

    static volatile ConcurrentHashMap<Long, Channel> channelMap;
    private static final Object lock = new Object();


    public static ConcurrentHashMap<Long, Channel> getChannelMap() {
        if (null == channelMap) {
            synchronized (lock) {
                if (null == channelMap) {
                    channelMap = new ConcurrentHashMap<>();
                }
            }
        }
        return channelMap;
    }

    public static Channel getChannel(Long userId) {
        if (null == channelMap) {
            return getChannelMap().get(userId);
        }
        return channelMap.get(userId);
    }

    public static void remove(Channel channel) {
        if (channel != null) {
            AttributeKey<Long> key = AttributeKey.valueOf("userId");
            Attribute<Long> value = channel.attr(key);
            //移除映射
            send(channel, MessageAction.CLOSE_CHANNEL);
            if (value.get() != null) {
                getChannelMap().remove(value.get());
                log.debug("[关闭channel], userId: {}", value.get());
            }else {
                log.debug("[关闭channel], channelId: {}", channel.id());
            }
            channel.close();
        }
    }

    /**
     * 通过ID发送消息
     */
    public static <T> Boolean send(Long userId, Integer action, T content) {
        Channel channel = getChannel(userId);
        return send(channel, action, content);
    }

    public static Boolean send(Channel channel, MessageAction action) {
        return send(channel, action.ACTION, action.CONTENT);
    }

    /**
     * 通过channel发送消息
     * 对空Channel进行了处理，不用担心
     */
    public static <T> Boolean send(Channel channel, Integer action, T content) {
        if (channel != null) {
            Notice notice = Notice.builder()
                    .action(action)
                    .content(JsonUtil.toJson(content))
                    .build();
            channel.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(notice)));
            return true;
        }
        return false;
    }

}
