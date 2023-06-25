package com.example.base.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BeanCopyUtils {

    private BeanCopyUtils() {}

    public static <T> T copy(Object source, Class<T> clazz) {
        if (clazz != null) {
            try {
                T target = clazz.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(source, target);
                return target;
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        throw new NullPointerException("clazz对象不能为空");
    }

    public static <T> List<T> copyList(List<? extends Object> source, Class<T> clazz) {
        if (clazz != null) {
            return source.stream().map(o -> copy(o, clazz)).collect(Collectors.toList());
        }
        throw new NullPointerException("clazz对象不能为空");
    }
}
