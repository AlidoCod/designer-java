package com.example.base.service;

import com.example.base.bean.dto.SysUserUpdateDto;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.vo.SysUserVo;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.repository.SysUserRepository;
import com.example.base.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

    final SysUserRepository userRepository;
    final RedisStringClient redisStringClient;
    final JsonService jsonService;
    //@CacheEvict(value = RedisConstant.CACHE_USER_ID, key = "#root.args[0]")
    public void update(Long id, SysUserUpdateDto updateDto) {
        SysUserVo user = BeanCopyUtils.copy(updateDto, SysUserVo.class);
        user.setId(id);
        //int nums = userRepository.updateById(user);
        //缓存入Redis
        redisStringClient.set(RedisConstant.UPDATE_USER_SET + id, jsonService.toJson(user));
    }

    @Cacheable(value = RedisConstant.CACHE_USER_ID, key = "#root.args[0]")
    public SysUserVo query(Long id) {
        SysUser sysUser = userRepository.selectById(id);
        return BeanCopyUtils.copy(sysUser, SysUserVo.class);
    }
}
