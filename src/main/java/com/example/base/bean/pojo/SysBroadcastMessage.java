package com.example.base.bean.pojo;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Data
public class SysBroadcastMessage {

    Long id;
    String title;
    String message;
    LocalDateTime time;
}
