package com.example.base;

import com.example.base.bean.Test;
import com.example.base.bean.TestDto;
import com.example.base.util.BeanCopyUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@Slf4j
@SpringBootTest
public class BeanCopyUtilsTest {

    @org.junit.jupiter.api.Test
    public void test() {
        TestDto testDto = new TestDto();
        testDto.setUsername("test");
        Test test = BeanCopyUtils.copy(testDto, Test.class);
        log.info(test.toString());
    }

    @org.junit.jupiter.api.Test
    public void list() {
        List<TestDto> source = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestDto testDto = new TestDto();
            testDto.setUsername("test");
            source.add(testDto);
        }
        List<Test> target = BeanCopyUtils.copyList(source, Test.class);
        target.forEach(o -> log.info(o.toString()));
    }
}
