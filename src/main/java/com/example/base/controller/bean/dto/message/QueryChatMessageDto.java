package com.example.base.controller.bean.dto.message;

import com.example.base.controller.bean.dto.base.BasePage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QueryChatMessageDto extends BasePage {

    @NotNull
    Long senderId;
    @NotNull
    Long receiverId;
}
