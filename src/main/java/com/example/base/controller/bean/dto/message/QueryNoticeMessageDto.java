package com.example.base.controller.bean.dto.message;

import com.example.base.controller.bean.dto.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true)
@Data
public class QueryNoticeMessageDto extends BasePage {

    @NonNull
    Long receiverId;
}
