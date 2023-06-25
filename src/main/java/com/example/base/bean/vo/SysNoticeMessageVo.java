package com.example.base.bean.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysNoticeMessageVo {

    String title;
    String message;
    LocalDateTime createTime;
}
