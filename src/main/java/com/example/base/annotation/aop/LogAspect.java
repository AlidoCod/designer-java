package com.example.base.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.aspectj.lang.reflect.*;

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

    @Around(value = "@annotation(com.example.base.annotation.Log)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        log.debug("==begin");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取反射方法
        Method method = signature.getMethod();
        // 获取注解
        Log annotation = method.getAnnotation(Log.class);
        String[] messages = annotation.messages();

        // 1. 获取请求类+方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        String classAndMethodName = className + "_" + methodName;

        log.debug("[request path]: {}()", classAndMethodName);

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
        log.debug("[extra message]: {}", Arrays.toString(messages));
        log.debug("==end");
        return object;
    }

}
