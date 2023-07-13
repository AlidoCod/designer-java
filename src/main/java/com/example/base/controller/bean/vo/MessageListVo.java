package com.example.base.controller.bean.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageListVo {

    Long userId;
    String message;
    LocalDateTime createTime;
}
