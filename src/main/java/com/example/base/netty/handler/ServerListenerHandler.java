package com.example.base.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class ServerListenerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     *  处理客户端传来的消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {

    }


}
