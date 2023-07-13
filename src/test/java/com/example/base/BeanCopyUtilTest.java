package com.example.base;

import com.example.base.bean.Test;
import com.example.base.bean.TestDto;
import com.example.base.bean.entity.SysMessage;
import com.example.base.utils.BeanCopyUtil;
import com.example.base.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
public class BeanCopyUtilTest {

    @org.junit.jupiter.api.Test
    public void test() {
        TestDto testDto = new TestDto();
        testDto.setUsername("test");
        Test test = BeanCopyUtil.copy(testDto, Test.class);
        log.info(test.toString());
    }

    @org.junit.jupiter.api.Test
    public void json() {
        List<SysMessage> list = List.of(SysMessage.sendSystemNoticeMessage(111L, "", ""), new SysMessage());
        System.out.println(JsonUtil.toJson(list));
    }

    @org.junit.jupiter.api.Test
    public void list() {
        List<TestDto> source = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestDto testDto = new TestDto();
            testDto.setUsername("test");
            source.add(testDto);
        }
        //List<Test> target = BeanCopyUtil.copyList(source, Test.class);
        List<Test> target = BeanCopyUtil.copy(source, List.class);
        target.forEach(o -> log.info(o.toString()));
    }
}
