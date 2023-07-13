package com.example.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.base.bean.entity.SysOrder;
import org.springframework.stereotype.Repository;

@Repository
public interface SysOrderRepository extends BaseMapper<SysOrder> {
}
