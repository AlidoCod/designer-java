package com.example.base.annotation;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 雷佳宝
 * 聚合注解，包装了请求映射、swagger文档、json返回，实现了日志切面。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//可以获取父类注解，聚合注解核心
@Inherited
//请求映射
@RequestMapping
//swagger文档生成
@Operation
//返回Json
@ResponseBody
public @interface Aggregation {

    /**
     *  请求路径
     */
    @AliasFor(
            annotation = RequestMapping.class,
            attribute = "path"
    )
    String[] path();

    /**
     *  请求方法
     */
    @AliasFor(
            annotation = RequestMapping.class,
            attribute = "method"
    )
    RequestMethod[] method();

    @AliasFor(
            annotation = Operation.class,
            attribute = "summary"
    )
    String summary() default "";

    @AliasFor(
            annotation = Operation.class,
            attribute = "description"
    )
    String description() default "";

    String[] messages() default {};

    /*
    * 默认最大访问次数
    * */
    int permitsPerSecond() default 10;

    long timeout() default 1000;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    boolean limit() default true;
}
