package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(value = "sys_demand")
@Builder
@Data
public class SysDemandSummary {

    @TableId
    Long id;
    Integer demandCondition;
    LocalDateTime deadTime;
}
