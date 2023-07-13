package com.example.base.service.plain;

import com.example.base.exception.GlobalRuntimeException;
import com.example.base.utils.JsonUtil;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Service
public class RabbitService {

    final RabbitTemplate rabbitTemplate;

    final BeanFactory beanFactory;

    public void toMsg(String exchange, String routing, Object object) {
        log.debug("发送MQ-MSG, exchange: {}, routing: {}", exchange, routing);
        rabbitTemplate.convertAndSend(exchange, routing, JsonUtil.toJson(object));
    }

    public <T> T toPojo(Message message, Class<T> clazz) {
        try {
            return JsonUtil.objectMapper.readValue(message.getBody(), clazz);
        } catch (IOException e) {
            log.error("", e);
            throw GlobalRuntimeException.of("Json转换失败");
        }
    }

    public <T> void accept(Message message, Channel channel, Class<T> clazz, Consumer<T> consumer) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            T pojo = beanFactory.getBean(RabbitService.class).toPojo(message, clazz);
            log.debug("消费MQ, pojo: {}", pojo);
            consumer.accept(pojo);
            //multiple(批量签收): 一条条签收
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.warn("消息签收失败", e);
            //错误消息是否重回队列
            try {
                channel.basicNack(deliveryTag, true, false);
            } catch (IOException ex) {
                log.warn("拒绝消息错误", ex);
            }
        }
    }
}
