package com.example.base.netty.handler;

import com.example.base.bean.vo.result.Result;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.netty.pojo.DataContent;
import com.example.base.netty.pojo.MessageAction;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.SysMessageRepository;
import com.example.base.service.JsonService;
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
import java.util.Set;

@ChannelHandler.Sharable
@Slf4j
@RequiredArgsConstructor
@Component
public class ServerListenerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    final JsonService jsonService;
    final SysMessageRepository messageRepository;
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
        DataContent dataContent = jsonService.toPojo(content, DataContent.class);
        assert dataContent != null;
        Channel channel = ctx.channel();
        Integer action = dataContent.getAction();
        /**
         * 连接消息的处理
         */
        if(Objects.equals(action, MessageAction.CONNECT.type)){
            //进行关联注册
            Long userid = dataContent.getUserId();
            AttributeKey<Long> key = AttributeKey.valueOf("userId");
            ctx.channel().attr(key).setIfAbsent(userid);
            UserConnectPool.getChannelMap().put(userid,channel);
            //发送离线未读消息
            messageReminder(userid, channel);
        }
        /**
         * 心跳处理
         */
        else if(Objects.equals(action, MessageAction.KEEPALIVE.type)){
            /**
             * 心跳包的处理
             * */
            log.debug("收到来自channel 为["+channel+"]的心跳包"+dataContent);
            channel.writeAndFlush(new TextWebSocketFrame(
                    jsonService.toJson(Result.success("已收到心跳包...返回心跳包"))
            ));
            log.debug("已返回心跳包");
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //接收到请求
        log.debug("有新的客户端链接：[{}]", ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String chanelId = ctx.channel().id().asLongText();
        log.debug("客户端被移除：channel id 为："+chanelId);
        removeUserId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //发生了异常后关闭连接，同时从channelGroup移除
        ctx.channel().close();
        removeUserId(ctx);
    }

    /**
     * 删除用户与channel的对应关系
     */
    private void removeUserId(ChannelHandlerContext ctx) {
        AttributeKey<Long> key = AttributeKey.valueOf("userId");
        Long userId = ctx.channel().attr(key).get();
        UserConnectPool.getChannelMap().remove(userId);
    }

    /**
     * 提醒总计未读消息
     * @param userId
     * @param channel
     */
    private void messageReminder(Long userId, Channel channel) {
        Long notice = redisStringClient.get(RedisConstant.NOTICE_ID + userId, Long.class);
        if (notice != 0L) {
            String s = String.format("你有%d条通知未读!", notice);
            channel.writeAndFlush(new TextWebSocketFrame(jsonService.toJson(Result.success(s))));
        }
        Set<String> chats = redisStringClient.keys(RedisConstant.CHAT + "*::" + userId);
        long chat = 0L;
        for (String key : chats) {
            chat += redisStringClient.get(key, Long.class);
        }
        if (chat != 0L) {
            String s = String.format("你有%d条聊天消息未读!", chat);
            channel.writeAndFlush(new TextWebSocketFrame(jsonService.toJson(Result.success(s))));
        }
    }

}
