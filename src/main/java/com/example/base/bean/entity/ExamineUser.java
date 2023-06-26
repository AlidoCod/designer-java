package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "examine_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ExamineUser extends BaseEntity {

    Long cheatId;
    String email;
    String tag;
    String favour;
    String nickname;
    Long avatar;
    String signature;
    String message;
    Integer examineCondition;
}
