package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "resource")
@EqualsAndHashCode(callSuper = true)
@Data
public class Resource extends BaseEntity {
}
