package com.example.base.service.user;

import com.example.base.bean.entity.ExamineUser;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.user.SysUserUpdateDto;
import com.example.base.controller.bean.vo.SysUserVo;
import com.example.base.repository.ExamineUserRepository;
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
    final ExamineUserRepository examineUserRepository;

    public void update(Long id, SysUserUpdateDto updateDto) {
        ExamineUser copy = BeanCopyUtils.copy(updateDto, ExamineUser.class);
        //设置审核状态
        copy.setExamineCondition(ExamineCondition.ING);
        copy.setId(null);
        copy.setCheatId(id);
        examineUserRepository.insert(copy);
    }

    @Cacheable(value = RedisConstant.CACHE_USER_ID, key = "#root.args[0]")
    public SysUserVo query(Long id) {
        SysUser sysUser = userRepository.selectById(id);
        return BeanCopyUtils.copy(sysUser, SysUserVo.class);
    }
}
