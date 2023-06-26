package com.example.base.controller.bean.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysMessageVo {

    Long senderId;
    Long receiveId;
    Long annexId;
    String title;
    String message;
    LocalDateTime createTime;
}
