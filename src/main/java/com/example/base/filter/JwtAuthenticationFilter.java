package com.example.base.filter;

import com.example.base.bean.entity.SysUser;
import com.example.base.bean.pojo.JwtUser;
import com.example.base.service.plain.JwtService;
import com.example.base.util.BeanCopyUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * 需要每次收到请求的时候，过滤器都处于活动状态
     * 因此每次用户发送请求时希望过滤器被触发并完成要做的所有工作
     */
    private final JwtService jwtService;
 
    /**
     * 总体流程：
     * 如果我们有我们的用户电子邮箱并且用户未通过身份验证，我们会从数据库中获取用户详细信息（loadUserByUsername --> UserDetails）
     * 然后我们需要做的是检查用户是否有效，如果用户和令牌有效，我们创建一个UsernamePasswordAuthenticationToken对象，传递UserDetails & 凭证 & 权限信息
     * 扩展上面生成的authToken，包含我们请求的详细信息，然后更新安全上下文中的身份验证令牌
     * 最后一步执行过滤器chain，别忘记放行
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取认证信息
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final JwtUser user;
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        // 从token中解析出username
        user = jwtService.parseToken(jwt);
        if (user != null && SecurityContextHolder.getContext().getAuthentication() == null){
            SysUser sysUser = BeanCopyUtils.copy(user, SysUser.class);
            //log.debug("user: {}", sysUser);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    sysUser,
                    // 用户凭证
                    null,
                    sysUser.getAuthorities());
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            // 更新安全上下文的持有用户
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}