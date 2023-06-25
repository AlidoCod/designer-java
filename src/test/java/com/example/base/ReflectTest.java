package com.example.base;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ReflectTest {

    @Test
    public void test() {
        for (Field field : com.example.base.bean.Test.class.getFields()) {
            System.out.println(field.getName());
        }
    }

    public static void main(String[] args) {
        for (Field field : com.example.base.bean.Test.class.getDeclaredFields()) {
            field.setAccessible(true);
            System.out.println(field.getName());
        }
    }
}
