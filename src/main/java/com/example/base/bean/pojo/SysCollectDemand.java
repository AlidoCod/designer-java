package com.example.base.bean.pojo;

import com.example.base.bean.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SysCollectDemand extends BaseEntity {

    Long demandId;
    Long userId;
}
