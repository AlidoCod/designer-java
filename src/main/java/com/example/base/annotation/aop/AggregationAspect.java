package com.example.base.annotation.aop;

import com.example.base.annotation.Aggregation;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.exception.TooManyRequestsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Order(value = 1)
@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class AggregationAspect {

    private final ObjectMapper objectMapper;

    final Map<String, RateLimiter> limitMap = new ConcurrentHashMap<>();

    @Around(value = "@annotation(com.example.base.annotation.Aggregation)")
    public Object aggregation(ProceedingJoinPoint joinPoint) throws Throwable {
        //限流
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Aggregation annotation = method.getAnnotation(Aggregation.class);
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        if (annotation != null && annotation.limit()) {
            String key = className + methodName;
            RateLimiter value;
            //若不包含就创建
            if (!limitMap.containsKey(key)) {
                value = RateLimiter.create(annotation.permitsPerSecond());
                limitMap.put(key, value);
            }
            value = limitMap.get(key);
            boolean acquire = value.tryAcquire(annotation.timeout(), annotation.timeUnit());
            if (!acquire) {
                log.debug("ip: {}, 获取令牌桶失败, 被限流", key);
                throw TooManyRequestsException.of();
            }
        }

        log.debug("==begin");
        log.debug("[request class]: {}", className);
        assert annotation != null;
        log.debug("[request path]: /{}", (Object) Optional.ofNullable(annotation.path()).orElseThrow(() -> GlobalRuntimeException.of("")));

        //获取请求参数, JSON解析前的参数
        Object[] args = joinPoint.getArgs();
        List<Object> argsList=new ArrayList<>();
        for (Object arg : args) {
            // 排除这两种参数类型
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse || arg instanceof MultipartFile) {
                continue;
            }
            argsList.add(arg);
        }
        String argsJson = objectMapper.writeValueAsString(argsList);

        log.debug("[request params]: {}", argsJson);
        //程序执行时间
        long begin = System.currentTimeMillis();
        Object object = joinPoint.proceed();
        log.debug("[exec time]: {}ms", System.currentTimeMillis() - begin);
        String[] messages = annotation.messages();
        if (messages.length != 0) {
            log.debug("[messages]: {}", Arrays.toString(messages));
        }
        log.debug("==end");
        return object;
    }
}
