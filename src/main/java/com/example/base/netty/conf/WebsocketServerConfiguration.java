package com.example.base.netty.conf;

import com.example.base.netty.handler.ServerChannelHandlerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@EnableConfigurationProperties
@Configuration
public class WebsocketServerConfiguration {

    final NettyProperties nettyProperties;

    final ServerChannelHandlerInitializer serverChannelHandlerInitializer;
    /**
     * boss 线程池
     * 负责客户端连接
     * @return
     */
    @Bean
    public NioEventLoopGroup boosGroup(){
        return new NioEventLoopGroup(nettyProperties.getBoss());
    }

    /**
     * worker线程池
     * 负责业务处理
     * @return
     */
    @Bean
    public NioEventLoopGroup workerGroup(){
        return new NioEventLoopGroup(nettyProperties.getWorker());
    }
    /**
     * 服务器启动器
     * @return
     */
    @Bean
    public ServerBootstrap serverBootstrap(@Qualifier(value = "boosGroup") NioEventLoopGroup boosGroup, @Qualifier(value = "workerGroup") NioEventLoopGroup workerGroup){
        ServerBootstrap serverBootstrap  = new ServerBootstrap();
        serverBootstrap
                .group(boosGroup, workerGroup)   // 指定使用的线程组
                .channel(NioServerSocketChannel.class) // 指定使用的通道
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyProperties.getTimeout()) // 指定连接超时时间
                .childHandler(serverChannelHandlerInitializer) // 指定worker处理器
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        return serverBootstrap;
    }

}
