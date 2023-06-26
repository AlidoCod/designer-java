package com.example.base.constant;

public class RedisConstant {

    public static final String VERIFY_CODE_IP = "VERIFY:CODE:IP::";
    public static final String CACHE_USER_ID = "CACHE:USER:ID";
    public static final String CACHE_DEMAND_ID = "CACHE:DEMAND:ID";
    public static final String BROADCAST_MESSAGE = "BROADCAST:MESSAGE:ID::";
    public static final String NOTICE_ID = "NOTICE:ID::";
    public static final String CHAT_FORMAT = "CHAT::%d::%d";
    public static final String CHAT = "CHAT::";

    public static final String BROADCAST_BITMAP = "BROADCAST:BITMAP::";

    public static String chatKeyFormat(Long senderId, Long receiverId) {
        return String.format(CHAT_FORMAT, senderId, receiverId);
    }
}
