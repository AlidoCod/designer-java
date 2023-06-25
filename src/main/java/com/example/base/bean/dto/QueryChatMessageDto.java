package com.example.base.bean.dto;

import com.example.base.bean.dto.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QueryChatMessageDto extends BasePage {

    Long senderId;
    Long receiverId;
}
