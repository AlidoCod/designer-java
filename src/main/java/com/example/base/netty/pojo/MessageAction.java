package com.example.base.netty.pojo;

public enum MessageAction {

    //定义消息类型
    CONNECT(1,"连接初始化成功!"),
    KEEPALIVE(2,"PONG!"),
    NOTICE_CLOSE(3, "由于未发送心跳包，连接已关闭。请重新连接。"),
    NOTICE_UNREAD_NUMS(4, "未读通知数量。"),
    CHAT_UNREAD_NUMS(5, "聊天消息未读数量。"),
    NOTICE_MESSAGE(6, "在线通知消息。"),
    CHAT_MESSAGE(7, "在线聊天消息。"),
    USER_COOPERATE_MESSAGE(8, "用户合作请求"),
    DESIGNER_COOPERATE_MESSAGE(9, "设计师合作请求"),
    DESIGNER_CHAT_COOPERATE_MESSAGE(10, "聊天框合作请求"),
    CLOSE_CHANNEL(11, "关闭连接");

    public final Integer ACTION;
    public final String CONTENT;

    MessageAction(Integer ACTION, String CONTENT) {
        this.ACTION = ACTION;
        this.CONTENT = CONTENT;
    }
}

