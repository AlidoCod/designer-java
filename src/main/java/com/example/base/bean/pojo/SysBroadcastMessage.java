package com.example.base.bean.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class SysBroadcastMessage {

    @JsonIgnore
    public static volatile AtomicLong atomic = new AtomicLong(0L);

    Long id;
    String title;
    String message;
    LocalDateTime time;
}
