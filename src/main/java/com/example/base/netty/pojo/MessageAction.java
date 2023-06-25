package com.example.base.netty.pojo;

public enum MessageAction {

    //定义消息类型

    CONNECT(1,"第一次（或重连）初始化连接"),
    CHAT(2,"聊天消息"),
    SIGNED(3,"消息签收"),
    KEEPALIVE(4,"客户端保持心跳");

    public final Integer type;
    public final String content;
    MessageAction(Integer type, String content) {
        this.type = type;
        this.content = content;
    }
}

