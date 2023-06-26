package com.example.base.service.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.ExamineUser;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.constant.GenericConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.user.RejectSysUserUpdateDto;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.ExamineUserRepository;
import com.example.base.repository.SysMessageRepository;
import com.example.base.repository.SysUserRepository;
import com.example.base.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerSysUserService{

    final SysUserRepository userRepository;
    final SysMessageRepository messageRepository;
    final ApplicationContext applicationContext;
    final ManagerSysMessageService sysMessageService;
    final ExamineUserRepository examineUserRepository;
    public Page<ExamineUser> query(BasePage basePage) {
        Page<ExamineUser> page = basePage.<ExamineUser>getPage();
        page = examineUserRepository.selectPage(page,
                Wrappers.<ExamineUser>lambdaQuery()
                        .eq(ExamineUser::getExamineCondition, ExamineCondition.ING)
        );
        return page;
    }


    final Object lock = new Object();
    /**
     * 确认更新, 线程安全
     * 确认后缓存要记得删除
     */
    @Transactional
    @CacheEvict(value = RedisConstant.CACHE_USER_ID, key = "#root.args[0]")
    public void accept(Long id) {
        SysUser user;
        //上锁，避免重复更新
        synchronized (lock) {
            ExamineUser examineUser = examineUserRepository.selectById(id);
            if (ExamineCondition.ING.equals(examineUser.getExamineCondition())) {
                user = BeanCopyUtils.copy(examineUser, SysUser.class);
                //设置真ID
                user.setId(examineUser.getCheatId());
                //更新原数据库数据
                userRepository.updateById(user);
                //修改审核状态
                ExamineUser update = new ExamineUser();
                update.setExamineCondition(ExamineCondition.YES);
                update.setId(id);
                examineUserRepository.updateById(update);
            }
            else {
                throw GlobalRuntimeException.of("请勿重复点击，多线程重复更新!");
            }
        }
    }

    /**
     * 拒绝更新
     */
    @Transactional
    public void reject(RejectSysUserUpdateDto rejectSysUserUpdateDto) {
        //包装成通知
        SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(rejectSysUserUpdateDto.getUserId(), GenericConstant.NOT_EXAMINE, rejectSysUserUpdateDto.getMessage());
        sysMessageService.publishSysNoticeMessage(sysMessage);
        //修改审核状态
        ExamineUser examineUser = new ExamineUser();
        examineUser.setId(rejectSysUserUpdateDto.getId());
        examineUser.setMessage(rejectSysUserUpdateDto.getMessage());
        examineUser.setExamineCondition(ExamineCondition.NO);
        examineUserRepository.updateById(examineUser);
    }

}
