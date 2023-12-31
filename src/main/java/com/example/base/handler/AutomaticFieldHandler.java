package com.example.base.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.handlers.StrictFill;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * mp 开始/更新时间处理器
 */
@Slf4j
@Component
public class AutomaticFieldHandler implements MetaObjectHandler {

    /**
     * 自动填充创建时间
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("found insert event, auto fill create_time/update_time field");
        //避免时钟发生变化
        LocalDateTime now = LocalDateTime.now();
        StrictFill<LocalDateTime, LocalDateTime> createTime = StrictFill.of("createTime", LocalDateTime.class, now);
        StrictFill<LocalDateTime, LocalDateTime> updateTime = StrictFill.of("updateTime", LocalDateTime.class, now);
        this.strictInsertFill(findTableInfo(metaObject), metaObject, List.of(createTime, updateTime));
    }

    /**
     * 自动填充更新时间
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("found update event, auto fill update_time field");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}