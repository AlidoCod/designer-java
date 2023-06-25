package com.example.base.util;

import com.example.base.exception.GlobalRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
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

    public static <T> T mapToBean(Map<String, Object> source, Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        try {
            T target = clazz.getDeclaredConstructor().newInstance();
            for (Field field : fields) {
                Object attribute;
                if ((attribute = source.get(field.getName())) != null) {
                    field.setAccessible(true);
                    field.set(target, attribute);
                    field.setAccessible(false);
                }
            }
            return target;
        } catch (Exception ex) {
            log.error("", ex);
            throw GlobalRuntimeException.fail();
        }
    }

    /**
     *  拷贝链表的方法，因为由于泛型擦除，只能传入List.clazz，因此无法拷贝对应的链表。
     */
    public static <T> List<T> copyList(List<? extends Object> source, Class<T> clazz) {
        if (clazz != null) {
            return source.stream().map(o -> copy(o, clazz)).collect(Collectors.toList());
        }
        throw new NullPointerException("clazz对象不能为空");
    }
}
