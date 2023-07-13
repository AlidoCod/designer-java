package com.example.base.conf;

import com.example.base.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitMQConfiguration {

    /* 正常配置 **********************************************************************************************************/

    /**
     * 正常交换机，开启持久化
     */
    @Bean
    DirectExchange designerExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.DESIGNER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue ttlQueue() {
        Map<String, Object> args = deadQueueArgs();
        return QueueBuilder.durable(RabbitMQConstant.TTL_QUEUE)
                .ttl(30 * 60 * 1000)
                .withArguments(args)
                .build();
    }

    @Bean
    Binding normalRouteBinding() {
        return BindingBuilder.bind(ttlQueue()).to(designerExchange()).with(RabbitMQConstant.TTL_ROUTE);
    }

    /* 推荐配置 **********************************************************************************************************/
    @Bean
    DirectExchange DESIGNER_RECOMMEND_EXCHANGE() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.DESIGNER_RECOMMEND_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue DESIGNER_RECOMMEND_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.DESIGNER_RECOMMEND_QUEUE)
                .ttl(30 * 60 * 1000)
                .build();
    }

    @Bean
    Binding DESIGNER_RECOMMEND_QUEUE_BINDING() {
        return BindingBuilder.bind(DESIGNER_RECOMMEND_QUEUE())
                .to(DESIGNER_RECOMMEND_EXCHANGE())
                .with(RabbitMQConstant.DESIGNER_RECOMMEND_QUEUE);
    }
    /* 死信配置 **********************************************************************************************************/

    /**
     * 死信交换机
     */
    @Bean
    DirectExchange deadExchange() {
        return new DirectExchange(RabbitMQConstant.DEAD_EXCHANGE, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadQueue() {
        return new Queue(RabbitMQConstant.DEAD_QUEUE, true, false, false);
    }

    @Bean
    Binding deadRouteBinding() {
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with(RabbitMQConstant.DEAD_ROUTE);
    }

    /**
     * 转发到 死信队列，配置参数
     */
    private Map<String, Object> deadQueueArgs() {
        Map<String, Object> map = new HashMap<>();
        // 绑定该队列到死信交换机
        map.put("x-dead-letter-exchange", RabbitMQConstant.DEAD_EXCHANGE);
        map.put("x-dead-letter-routing-key", RabbitMQConstant.DEAD_ROUTE);
        return map;
    }

    /* ES交换机 **********************************************************************************************************/
    @Bean
    DirectExchange DESIGNER_INSERT_EXCHANGE() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.DESIGNER_INSERT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    DirectExchange DESIGNER_UPDATE_EXCHANGE() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    DirectExchange DESIGNER_DELETE_EXCHANGE() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.DESIGNER_DELETE_EXCHANGE)
                .durable(true)
                .build();
    }

    /* 队列 **********************************************************************************************************/
    @Bean
    public Queue USER_INSERT_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.USER_INSERT_QUEUE).build();
    }

    @Bean
    public Queue USER_DELETE_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.USER_DELETE_QUEUE).build();
    }

    @Bean
    public Queue USER_UPDATE_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.USER_UPDATE_QUEUE).build();
    }


    @Bean
    public Queue DEMAND_INSERT_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.DEMAND_INSERT_QUEUE).build();
    }

    @Bean
    public Queue DEMAND_DELETE_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.DEMAND_DELETE_QUEUE).build();
    }

    @Bean
    public Queue DEMAND_UPDATE_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.DEMAND_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue WORKS_INSERT_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.WORKS_INSERT_QUEUE).build();
    }

    @Bean
    public Queue WORKS_DELETE_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.WORKS_DELETE_QUEUE).build();
    }

    @Bean
    public Queue WORKS_UPDATE_QUEUE() {
        return QueueBuilder.durable(RabbitMQConstant.WORKS_UPDATE_QUEUE).build();
    }

    /* 绑定 **********************************************************************************************************/

    //用户
    @Bean
    public Binding USER_INSERT_QUEUE_BINDING() {
        return BindingBuilder.bind(USER_INSERT_QUEUE())
                .to(DESIGNER_INSERT_EXCHANGE())
                .with(RabbitMQConstant.USER_INSERT_QUEUE);
    }

    @Bean
    public Binding USER_DELETE_QUEUE_BINDING() {
        return BindingBuilder.bind(USER_DELETE_QUEUE())
                .to(DESIGNER_DELETE_EXCHANGE())
                .with(RabbitMQConstant.USER_DELETE_QUEUE);
    }

    @Bean
    public Binding USER_UPDATE_QUEUE_BINDING() {
        return BindingBuilder.bind(USER_UPDATE_QUEUE())
                .to(DESIGNER_UPDATE_EXCHANGE())
                .with(RabbitMQConstant.USER_UPDATE_QUEUE);
    }

    //需求
    @Bean
    public Binding DEMAND_INSERT_QUEUE_BINDING() {
        return BindingBuilder.bind(DEMAND_INSERT_QUEUE())
                .to(DESIGNER_INSERT_EXCHANGE())
                .with(RabbitMQConstant.DEMAND_INSERT_QUEUE);
    }

    @Bean
    public Binding DEMAND_DELETE_QUEUE_BINDING() {
        return BindingBuilder.bind(DEMAND_DELETE_QUEUE())
                .to(DESIGNER_DELETE_EXCHANGE())
                .with(RabbitMQConstant.DEMAND_DELETE_QUEUE);
    }

    @Bean
    public Binding DEMAND_UPDATE_QUEUE_BINDING() {
        return BindingBuilder.bind(DEMAND_UPDATE_QUEUE())
                .to(DESIGNER_UPDATE_EXCHANGE())
                .with(RabbitMQConstant.DEMAND_UPDATE_QUEUE);
    }

    //作品
    @Bean
    public Binding WORKS_INSERT_QUEUE_BINDING() {
        return BindingBuilder.bind(WORKS_INSERT_QUEUE())
                .to(DESIGNER_INSERT_EXCHANGE())
                .with(RabbitMQConstant.WORKS_INSERT_QUEUE);
    }

    @Bean
    public Binding WORKS_DELETE_QUEUE_BINDING() {
        return BindingBuilder.bind(WORKS_DELETE_QUEUE())
                .to(DESIGNER_DELETE_EXCHANGE())
                .with(RabbitMQConstant.WORKS_DELETE_QUEUE);
    }

    @Bean
    public Binding WORKS_UPDATE_QUEUE_BINDING() {
        return BindingBuilder.bind(WORKS_UPDATE_QUEUE())
                .to(DESIGNER_UPDATE_EXCHANGE())
                .with(RabbitMQConstant.WORKS_UPDATE_QUEUE);
    }
}
