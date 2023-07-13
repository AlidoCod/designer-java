package com.example.base.netty.handler;

import com.example.base.netty.pojo.MessageAction;
import com.example.base.netty.pojo.UserConnectPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        //如果触发的事件是心跳事件，才进行处理
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.ALL_IDLE) {
                log.debug("一段时间内，未读取到读心跳和写心跳，连接超时...");
                Channel channel = ctx.channel();
                //发送连接关闭消息
                UserConnectPool.send(channel, MessageAction.NOTICE_CLOSE);
                //移除channel
                UserConnectPool.remove(channel);
            }
        }
    }
}
