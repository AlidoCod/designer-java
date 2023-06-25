package com.example.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.base.bean.entity.SysMessage;
import org.springframework.stereotype.Repository;

@Repository
public interface SysMessageRepository extends BaseMapper<SysMessage> {
}
