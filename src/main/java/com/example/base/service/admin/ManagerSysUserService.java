package com.example.base.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.dto.ConfirmSysUserInfoDto;
import com.example.base.bean.dto.base.BasePage;
import com.example.base.bean.entity.SysMessage;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.vo.SysUserVo;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.repository.SysMessageRepository;
import com.example.base.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerSysUserService{

    final RedisStringClient redisStringClient;
    final SysUserRepository userRepository;
    final SysMessageRepository messageRepository;
    final ApplicationContext applicationContext;
    final ManagerSysMessageService sysMessageService;
    public Page query(BasePage basePage) {
        List<SysUserVo> list = redisStringClient.gets(RedisConstant.UPDATE_USER_SET + "*", SysUserVo.class);
        log.debug(String.valueOf(list.size()));
        Page page = basePage.getPage();
        page.setRecords(list);
        return page;
    }

    /**
     * 确认更新
     * 确认后缓存要记得删除
     * @param id
     */
    @CacheEvict(value = RedisConstant.CACHE_USER_ID, key = "#root.args[0]")
    public void confirm(Long id) {
        SysUser user;
        //避免多线程重复更新
        synchronized (this) {
            user = redisStringClient.get(RedisConstant.UPDATE_USER_SET + id, SysUser.class);
            if (user == null) {
                return;
            }
            redisStringClient.delete(RedisConstant.UPDATE_USER_SET + id);
        }
        userRepository.updateById(user);
    }

    /**
     * 拒绝更新
     * @param confirmSysUserInfoDto
     */
    public void reject(ConfirmSysUserInfoDto confirmSysUserInfoDto) {
        SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(confirmSysUserInfoDto.getId(), confirmSysUserInfoDto.getTitle(), confirmSysUserInfoDto.getMessage());
        sysMessageService.publishSysNoticeMessage(sysMessage);
        redisStringClient.delete(RedisConstant.UPDATE_USER_SET + confirmSysUserInfoDto.getId());
    }

    public void confirm(ConfirmSysUserInfoDto messageDto) {
        ManagerSysUserService sysUserService = applicationContext.getBean(ManagerSysUserService.class);
        if (messageDto.getMessage() == null) {
            sysUserService.confirm(messageDto.getId());
        }else {
            sysUserService.reject(messageDto);
        }
    }

}
