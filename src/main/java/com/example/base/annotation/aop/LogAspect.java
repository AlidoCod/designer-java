package com.example.base.annotation.aop;

import com.example.base.annotation.Aggregation;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Order(value = 1)
@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class LogAspect {

    private final ObjectMapper objectMapper;

    @Around(value = "@annotation(com.example.base.annotation.Aggregation)")
    public Object aggregation(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("==begin");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取反射方法
        Method method = signature.getMethod();
        Aggregation annotation = method.getAnnotation(Aggregation.class);

        // 1. 获取请求类+方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();

        log.debug("[request class]: {}", className);
        log.debug("[request path]: /{}", methodName);

        // 2. 获取请求参数, JSON解析前的参数
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
        //4. 程序执行时间
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
