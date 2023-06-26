package com.example.base.service.plain;

import com.example.base.bean.entity.SysUser;
import com.example.base.bean.entity.enums.Role;
import com.example.base.bean.pojo.JwtUser;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.util.BeanCopyUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO 完成JWT的验证服务
 * JWT工具类
 *
 * @author 雷佳宝
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
 
    /**
     * 创建一个最终字符串，这个字符串称为密钥
     * JWT最低要求的安全级别是256bit
     */
    private static final String SECRET_KEY = "3F4428472B4B6250655368566D5971337336763979244226452948404D635166";
    final ObjectMapper objectMapper;
    static final String CLAIMS = "claims";
    static final String SUBJECT = "subject";
    static final int DEFAULT_OFFSET = 30;
    static final String TOKEN_PREFIX = "Bearer ";

    public String generateToken(SysUser sysUser) {
        Map<String, Object> claims = new HashMap<>();
        JwtUser user = BeanCopyUtils.copy(sysUser, JwtUser.class);
        claims.put(CLAIMS, user);
        Date now = new Date();
        String token = TOKEN_PREFIX +
                Jwts.builder()
                        .setClaims(claims)
                        .setSubject(SUBJECT)
                        .setIssuedAt(now)
                        .setExpiration(DateUtils.addDays(now, DEFAULT_OFFSET))
                        .signWith(SignatureAlgorithm.HS256, getSecretKey())
                        .compact();
        //log.debug("token生成成功: {}", token);
        return token;
    }

    public JwtUser parseToken(String token) {
        String msg;
        try {
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(getSecretKey())
                    //校验必须有这个属性，可以省略这步
                    .requireSubject(SUBJECT)
                    .parseClaimsJws(token);

            Claims claims = jws.getBody();
            //log.debug("claims: {}", claims.get(CLAIMS));
            Map map = claims.get(CLAIMS, Map.class);
            JwtUser user = new JwtUser();
            user.setId((Long) map.get("id"));
            user.setUsername((String) map.get("username"));
            user.setRole(Role.valueOf((String) map.get("role")));
            return user;
        }catch (SignatureException se) {
            msg = "密钥错误";
            log.error(msg, se);
            throw GlobalRuntimeException.of(msg);
        }catch (MalformedJwtException me) {
            msg = "密钥算法或者密钥转换错误";
            log.error(msg, me);
            throw GlobalRuntimeException.of(msg);

        }catch (MissingClaimException mce) {
            msg = "密钥缺少校验数据";
            log.error(msg, mce);
            throw GlobalRuntimeException.of(msg);

        }catch (ExpiredJwtException mce) {
            msg = "密钥已过期";
            log.error(msg, mce);
            throw GlobalRuntimeException.of(msg);

        }catch (JwtException jwte) {
            msg = "密钥解析错误";
            log.error(msg, jwte);
            throw GlobalRuntimeException.of(msg);
        }catch (Exception ex) {
            msg = "JSON解析失败";
            log.error(msg, ex);
            throw GlobalRuntimeException.of(msg);
        }
    }

    /**
     * 2、获取签名密钥的方法
     * @return 基于指定的密钥字节数组创建用于HMAC-SHA算法的新SecretKey实例
     */
    private byte[] getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return keyBytes;
    }

}