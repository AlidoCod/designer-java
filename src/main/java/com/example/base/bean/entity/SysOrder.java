package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "sys_order")
@EqualsAndHashCode(callSuper = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SysOrder extends BaseEntity {

    Long relateId;
    Long userId;
    String money;
    Integer isPay;
}
