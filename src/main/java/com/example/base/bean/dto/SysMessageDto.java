package com.example.base.bean.dto;

import lombok.Data;

@Data
public class SysMessageDto {

    Long receiverId;
    Long annexId;
    String title;
    String message;
}
