package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import com.example.base.handler.ListTypeHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@TableName(value = "sys_works")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SysWorks extends BaseEntity {

    Long designerId;
    Long demandId;
    @TableField(typeHandler = ListTypeHandler.class)
    List<Long> annexId;
    String title;
    String theme;
    String tag;
    String body;
    Integer grade;
    String evaluate;
}
