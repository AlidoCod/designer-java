package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@TableName(value = "sys_demand")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SysDemand extends BaseEntity {

    String title;
    String theme;
    String body;
    String technicalSelection;
    Long budget;
    Long annexId;
    Integer demandCondition;
}
