package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@TableName(value = "sys_cooperate_demand")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SysCooperateDemand extends BaseEntity {

    Long demandId;
    Long userId;
    Long designerId;
    Long money;
    LocalDateTime deadTime;
    Long worksId;
    Integer worksCondition;
    Integer paymentCondition;
}
