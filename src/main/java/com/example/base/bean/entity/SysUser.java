package com.example.base.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.base.bean.entity.base.BaseEntity;
import com.example.base.bean.entity.enums.Role;
import com.example.base.handler.RoleTypeHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SysUser extends BaseEntity implements UserDetails {

    String username;
    String password;
    //使用自定义类型处理器
    @TableField(typeHandler = RoleTypeHandler.class)
    Role role;
    String email;
    String tag;
    String favour;
    String nickname;
    Long avatar;
    String signature;
    /**
     * 权限控制
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     *  能通过jwt验证的一定不过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 用户没有锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     *  用户凭证
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     *  用户启用
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
