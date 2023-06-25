package com.example.base.netty.pojo;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class UserConnectPool {

    static volatile ConcurrentHashMap<Long, Channel> channelMap;
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();


    public static ConcurrentHashMap<Long, Channel> getChannelMap() {
        if (null == channelMap) {
            synchronized (lock2) {
                if (null == channelMap) {
                    channelMap = new ConcurrentHashMap<>();
                }
            }
        }
        return channelMap;
    }

    public static Channel getChannel(Long userId) {
        if (null == channelMap) {
            return getChannelMap().get(userId);
        }
        return channelMap.get(userId);
    }

}
