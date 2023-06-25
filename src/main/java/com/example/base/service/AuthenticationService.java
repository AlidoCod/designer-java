package com.example.base.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.base.bean.dto.RegisterDto;
import com.example.base.bean.entity.SysUser;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.RedisConstant;
import com.example.base.repository.SysUserRepository;
import com.example.base.util.BeanCopyUtils;
import com.example.base.util.IPUtils;
import com.example.base.util.VerifyCodeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author 雷佳宝
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    final JwtService jwtService;
    final SysUserRepository sysUserRepository;
    final PasswordEncoder passwordEncoder;
    final RedisStringClient redisStringClient;

    public String authenticate() {
        SysUser sysUser = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("principal: {}", sysUser);
        return jwtService.generateToken(sysUser);
    }

    static final String NICKNAME_PREFIX = "nick_";

    public String register(RegisterDto registerDto) {
        SysUser sysUser = BeanCopyUtils.copy(registerDto, SysUser.class);
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        sysUser.setNickname(NICKNAME_PREFIX + System.currentTimeMillis());
        log.debug("编码前: {}", registerDto.getPassword());
        log.debug("编码后: {}, 校验是否成功: {}", sysUser.getPassword(), passwordEncoder.matches(registerDto.getPassword(), sysUser.getPassword()));
        sysUserRepository.insert(sysUser);
        return jwtService.generateToken(sysUser);
    }

    public boolean isSysUserExist(String username) {
        SysUser sysUser = sysUserRepository.selectOne(new QueryWrapper<SysUser>().eq("username", username));
        return sysUser != null;
    }

    public void generateVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ip = IPUtils.getIpAddress(request);
        OutputStream os = response.getOutputStream();
        // 如果image为null，就发送图片、后再存入redis；否则则解码image、传送到前端
        String code = VerifyCodeUtils.generateVerifyCode(4);
        // 生成图片
        BufferedImage bufferedImage = VerifyCodeUtils.getBufferedImage(200, 80, code);
        // 发送图片
        ImageIO.write(bufferedImage, "jpg", os);
        // 存入redis
        redisStringClient.set(RedisConstant.VERIFY_CODE_IP + ip, code.toLowerCase(Locale.ROOT), 15L, TimeUnit.MINUTES);
        os.flush();
    }

    public boolean checkVerifyCode(HttpServletRequest request, String code) {
        String ip = IPUtils.getIpAddress(request);
        String source = redisStringClient.get(RedisConstant.VERIFY_CODE_IP + ip, String.class);
        if (source == null) {
            return false;
        }
        return source.equals(code.toLowerCase(Locale.ROOT));
    }
}
