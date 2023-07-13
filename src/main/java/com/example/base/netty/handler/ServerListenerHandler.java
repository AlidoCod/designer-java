package com.example.base.netty.handler;

import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.netty.pojo.DataContent;
import com.example.base.netty.pojo.MessageAction;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.utils.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ChannelHandler.Sharable
@Slf4j
@RequiredArgsConstructor
@Component
public class ServerListenerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    final SysMessageRepository sysMessageRepository;
    final ThreadPoolTaskExecutor executor;
    final RedisStringClient redisStringClient;

    static {
        //预加载
        UserConnectPool.getChannelMap();
    }

    /**
     *  处理客户端传来的消息
     */

    /**
     * 读取数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        /**
         * 1.接受到msg
         * 2.将msg转化为实体类
         * 3.解析消息类型
         * 将实体类当中的userid和连接的Channel进行对应
         * */
        String content = msg.text();
        DataContent dataContent = JsonUtil.toPojo(content, DataContent.class);
        assert dataContent != null;
        Channel channel = ctx.channel();
        Integer action = dataContent.getAction();
        /*
          连接消息的处理
         */
        if (Objects.equals(action, MessageAction.CONNECT.ACTION)) {
            //进行关联注册
            Long userId = dataContent.getUserId();
            //初始化channel
            initChannel(userId, channel);
            //发送离线未读消息
            messageReminder(userId);
        }
        /*
          心跳处理
         */
        else if (Objects.equals(action, MessageAction.KEEPALIVE.ACTION)) {
            /*
              心跳包的处理
              */
            //log.debug("收到来自channel 为[" + channel.id().asLongText() + "]的心跳包");
            UserConnectPool.send(channel, MessageAction.KEEPALIVE);
        }
    }

    private void initChannel(Long userId, Channel channel) {
        AttributeKey<Long> key = AttributeKey.valueOf("userId");
        channel.attr(key).setIfAbsent(userId);
        if (UserConnectPool.getChannel(userId) != null) {
            UserConnectPool.remove(channel);
        }
        //初始化连接
        UserConnectPool.getChannelMap().put(userId, channel);
        //发送初始化成功消息
        UserConnectPool.send(channel, MessageAction.CONNECT);
        log.debug("连接初始化成功，userId: {}", userId);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        //接收到请求
        log.debug("新的客户端连接, channelId: {}", ctx.channel().id());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        //正常移除channel
        UserConnectPool.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("", cause);
        //移除异常channel
        UserConnectPool.remove(ctx.channel());
    }

    /**
     * 提醒总计未读消息
     */
    private void messageReminder(Long userId) {
        /*
        * 通知未读数量
        * */
        Long noticeNums = 0L;
        //避免noticeNums为空
        noticeNums += Optional.ofNullable(redisStringClient.get(RedisConstant.NOTICE_ID + userId, Long.class)).orElse(0L);
        UserConnectPool.send(userId, MessageAction.NOTICE_UNREAD_NUMS.ACTION, noticeNums);

        /*
        * 聊天消息未读数量
        * */
        Set<String> chats = redisStringClient.keys(RedisConstant.CHAT + "*::" + userId);
        Long chatNums = 0L;
        for (String key : chats) {
            chatNums += Optional.ofNullable(redisStringClient.get(key, Long.class)).orElse(0L);
        }
        UserConnectPool.send(userId, MessageAction.CHAT_UNREAD_NUMS.ACTION, chatNums);
    }

}
