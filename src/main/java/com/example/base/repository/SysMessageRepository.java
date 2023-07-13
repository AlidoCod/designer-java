package com.example.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.base.bean.entity.SysMessage;
import com.example.base.controller.bean.vo.MessageListVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMessageRepository extends BaseMapper<SysMessage> {

    //distinct 避免子查询orderBy失效
    @Select("select receiver_id as userId, message, create_time as createTime from " +
            "(select distinct(receiver_id), message, create_time from sys_message " +
            "where sender_id = #{senderId} order by create_time desc) as t group by t.receiver_id")
    List<MessageListVo> selectListBySenderId(@Param("senderId") Long senderId);

    @Select("select sender_id as userId, message, create_time as createTime from " +
            "(select distinct(sender_id), message, create_time from sys_message " +
            "where receiver_id = #{receiverId} order by create_time desc) as t group by t.sender_id")
    List<MessageListVo> selectListByReceiverId(@Param("receiverId") Long receiverId);
}
