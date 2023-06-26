package com.example.base.controller.bean.dto.message;

import lombok.Data;

@Data
public class SysMessageDto {

    Long receiverId;
    Long annexId;
    String title;
    String message;
}
