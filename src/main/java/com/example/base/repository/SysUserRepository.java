package com.example.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.base.bean.entity.SysUser;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRepository extends BaseMapper<SysUser> {
}
