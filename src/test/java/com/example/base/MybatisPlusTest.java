package com.example.base;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.entity.enums.Role;
import com.example.base.repository.SysMessageRepository;
import com.example.base.repository.SysUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
@SpringBootTest
public class MybatisPlusTest {

    @Autowired
    SysUserRepository repository;

    @Autowired
    SysMessageRepository messageRepository;

    @Test
    public void insert() {
        SysUser sysUser = new SysUser();
        sysUser.setUsername("admin");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        sysUser.setPassword(bCryptPasswordEncoder.encode("123456"));
        sysUser.setRole(Role.ADMIN);
        repository.insert(sysUser);
    }

    @Test
    public void lambdaTest() {
        messageRepository.selectList(
                Wrappers.<SysMessage>lambdaQuery()
                        .orderByDesc(SysMessage::getCreateTime)
        ).stream().forEach(System.out::println);
    }
}
