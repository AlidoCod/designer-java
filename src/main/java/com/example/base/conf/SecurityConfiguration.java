package com.example.base.conf;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.entity.enums.Role;
import com.example.base.filter.JwtAuthenticationFilter;
import com.example.base.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    final SysUserRepository userRepository;

    /**
     *  loginPage: 对应的是登录页面，前后端分离后不需要配置。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   CorsConfigurationSource configurationSource
    ) throws Exception {
        http
                /*跨域配置*/
                .cors()
                .configurationSource(configurationSource)

                /*权限控制*/
                .and()
                //禁用csrf(防止跨站请求伪造攻击), token已防止
                .csrf()
                .disable()
                .authorizeHttpRequests()
                //进行角色权限控制
                .requestMatchers("/user/**").hasAuthority(Role.USER.name())
                .requestMatchers("/admin/**").hasAuthority(Role.ADMIN.name())
                //优先级低的规则, 覆盖所有
                .requestMatchers("/**")
                .permitAll()

                /*表单登录*/
                .and()
                .formLogin()
                //提交表单的目的URL
                .loginProcessingUrl("/login")
                //重定向返回数据的URL
                .successForwardUrl("/success")
                .failureForwardUrl("/failure")

                /*禁用缓存*/
                .and()
                .sessionManagement()
                // 使用无状态session，即不使用session缓存数据
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                /*添加功能*/
                .and()
                // 添加身份验证
                .authenticationProvider(authenticationProvider)
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                /*无状态jwt，不需要登出，登出由前端处理*/
        return http.build();
    }


    /**
     *  登录校验
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            SysUser sysUser = userRepository.selectOne(new QueryWrapper<SysUser>().eq("username", username));
            return Optional.ofNullable(sysUser).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        };
    }

    /**
     * 身份校验机制、身份验证提供程序
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        // 创建一个用户认证提供者
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 设置用户相信信息，可以从数据库中读取、或者缓存、或者配置文件
        authProvider.setUserDetailsService(userDetailsService);
        // 设置加密机制，若想要尝试对用户进行身份验证，我们需要知道使用的是什么编码
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * 基于用户名和密码或使用用户名和密码进行身份验证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 提供编码机制
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置跨域
     */
    @Bean
    public CorsConfigurationSource configurationSource() {
        UrlBasedCorsConfigurationSource source =   new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //同源配置，*表示任何请求都视为同源，若需指定ip和端口可以改为如“localhost：8080”，多个以“，”分隔；
        corsConfiguration.addAllowedOrigin("*");
        //header，允许哪些header，本案中使用的是token，此处可将*替换为token；
        corsConfiguration.addAllowedHeader("*");
        //允许的请求方法，PSOT、GET等
        corsConfiguration.addAllowedMethod("*");
        //配置允许跨域访问的url
        source.registerCorsConfiguration("/**",corsConfiguration);
        return source;
    }

}
