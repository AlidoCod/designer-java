package com.example.base.service.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.ExamineUser;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.bean.entity.enums.Role;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.GenericConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.user.RejectSysUserUpdateDto;
import com.example.base.controller.bean.vo.ExamineUserVo;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.ExamineUserRepository;
import com.example.base.repository.SysMessageRepository;
import com.example.base.repository.SysUserRepository;
import com.example.base.service.plain.RabbitService;
import com.example.base.utils.BeanCopyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamineUserService {

    final SysUserRepository userRepository;
    final SysMessageRepository messageRepository;
    final ApplicationContext applicationContext;
    final NoticeMessageService sysMessageService;
    final ExamineUserRepository examineUserRepository;
    public List<ExamineUserVo> query(BasePage basePage) {
        Page<ExamineUser> page = basePage.<ExamineUser>getPage();
        return examineUserRepository.selectPage(page,
                Wrappers.<ExamineUser>lambdaQuery()
                        .eq(ExamineUser::getExamineCondition, ExamineCondition.ING)
        ).getRecords().stream()
                .map(examineUser -> ExamineUserVo.getInstance(applicationContext, examineUser))
                .toList();
    }


    final Object lock = new Object();

    final RedisStringClient redisStringClient;
    final RabbitService rabbitService;
    final NoticeMessageService noticeMessageService;
    /**
     * 确认更新, 线程安全
     * 确认后缓存要记得删除
     * 记得发送ES
     */
    @Transactional
    public void accept(Long id) {
        SysUser user;
        //上锁，避免重复更新
        synchronized (lock) {
            ExamineUser examineUser = examineUserRepository.selectById(id);
            if (ExamineCondition.ING.equals(examineUser.getExamineCondition())) {
                user = BeanCopyUtil.copy(examineUser, SysUser.class);
                //设置真ID
                user.setId(examineUser.getCheatId());
                //更新原数据库数据
                userRepository.updateById(user);
                //删除缓存
                redisStringClient.delete(RedisConstant.CACHE_USER_ID + "::" + examineUser.getCheatId());
                //设置用户角色避免枚举为空出现异常
                user.setRole(Role.USER);
                //发送MQ到ES
                rabbitService.toMsg(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE, RabbitMQConstant.USER_UPDATE_QUEUE, user);
                //修改审核状态
                ExamineUser update = new ExamineUser();
                update.setExamineCondition(ExamineCondition.YES);
                update.setId(id);
                examineUserRepository.updateById(update);

                //发送消息通知用户信息更新成功
                String format = "尊敬的【%s】用户，您的个人信息已更新成功!";
                SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(user.getId(),
                        GenericConstant.SYSTEM_NOTICE + ": 个人信息审核已通过",
                        String.format(format, user.getNickname())
                );
                noticeMessageService.publishSysNoticeMessage(sysMessage);
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
