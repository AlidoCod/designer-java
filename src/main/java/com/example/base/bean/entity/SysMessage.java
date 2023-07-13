package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import com.example.base.bean.entity.enums.MessageCondition;
import com.example.base.bean.entity.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@TableName(value = "sys_message")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SysMessage extends BaseEntity {

    Long senderId;
    Long receiverId;
    Long annexId;
    String title;
    String message;
    Integer messageType;
    Integer messageCondition;

    public static SysMessage sendSystemNoticeMessage(Long receiverId, String title, String message) {
        return new SysMessage(0L, receiverId, 0L, title, message, MessageType.SYSTEM_NOTICE, MessageCondition.NOT_READ);
    }

    public static SysMessage sendNormalMessage(Long senderId, Long receiverId, Long annexId, String message) {
        return new SysMessage(senderId, receiverId, annexId, null, message, MessageType.CHAT_MESSAGE, MessageCondition.NOT_READ);
    }

    public void notSend() {
        this.messageCondition = MessageCondition.NOT_READ;
    }

    public void read() {
        this.messageCondition = MessageCondition.READ;
    }

}
