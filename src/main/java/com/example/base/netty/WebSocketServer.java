package com.example.base.netty;

import com.example.base.netty.conf.NettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketServer {

    final NettyProperties nettyProperties;
    final ServerBootstrap serverBootstrap;
    @Qualifier(value = "boosGroup")
    @Autowired
    NioEventLoopGroup boosGroup;
    @Qualifier(value = "workerGroup")
    @Autowired
    NioEventLoopGroup workerGroup;

    @PostConstruct
    public void start() throws InterruptedException {
        ChannelFuture future = serverBootstrap.bind(nettyProperties.getPort()).sync();
        if (future.isSuccess()) {
            log.info("WebSocket服务器在{}端口初始化启动成功", nettyProperties.getPort());
        }else {
            log.error("WebSocket服务器启动失败");
        }
    }

    @PreDestroy
    public void close() {
        boosGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        log.info("WebSocket服务器关闭成功, 线程组均已关闭");
    }
}
