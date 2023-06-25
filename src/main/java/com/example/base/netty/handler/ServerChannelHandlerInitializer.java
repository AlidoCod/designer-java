package com.example.base.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 初始化管道及管道处理器
 */
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
public class ServerChannelHandlerInitializer extends ChannelInitializer<SocketChannel> {

    final ServerListenerHandler serverListenerHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) {

        /*
        * Http请求处理
        * */
        ChannelPipeline pipeline = socketChannel.pipeline();
        //添加请求解码和响应编码
        pipeline.addLast(new HttpServerCodec());
        //异步写入大数据流
        pipeline.addLast(new ChunkedWriteHandler());
        //HTTP请求分块聚合
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));

        /*
        * 心跳处理
        * */
        //心跳事件触发
        //pipeline.addLast(new IdleStateHandler(8, 10, 12));
        //心跳事件处理器
        //pipeline.addLast(new HeartBeatHandler());

        /*
        * WebSocket处理
        * */
        //websocket协议处理器
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //协议事件处理器
        pipeline.addLast(serverListenerHandler);
    }
}
