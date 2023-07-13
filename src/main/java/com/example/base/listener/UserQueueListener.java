package com.example.base.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.example.base.constant.ElasticConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.document.SysUserDocument;
import com.example.base.service.plain.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserQueueListener {

    final RabbitService rabbitService;
    final ElasticsearchClient elasticsearchClient;

    @RabbitListener(queues = RabbitMQConstant.USER_INSERT_QUEUE)
    public void insertMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysUserDocument.class, sysUserDocument ->
                {
                    try {
                        IndexResponse response = elasticsearchClient.index(request ->
                                request.index(ElasticConstant.USER_INDEX)
                                        .id(String.valueOf(sysUserDocument.getId()))
                                        .document(sysUserDocument)
                        );
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstant.USER_DELETE_QUEUE)
    public void deleteMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysUserDocument.class, sysUserDocument ->
                {
                    try {
                        DeleteResponse response = elasticsearchClient.delete(request ->
                                request.index(ElasticConstant.USER_INDEX)
                                        .id(String.valueOf(sysUserDocument.getId()))
                        );
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstant.USER_UPDATE_QUEUE)
    public void updateMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysUserDocument.class, sysUserDocument ->
                {
                    try {
                        BooleanResponse exists = elasticsearchClient.exists(request ->
                                request.index(ElasticConstant.USER_INDEX)
                                        .id(String.valueOf(sysUserDocument.getId()))
                        );
                        //如果存在，则部分更新
                        if (exists.value()) {
                            log.debug("消费MQ，开始部分更新");
                            UpdateResponse<SysUserDocument> response = elasticsearchClient.update(request ->
                                            request.index(ElasticConstant.USER_INDEX)
                                                    .id(String.valueOf(sysUserDocument.getId()))
                                                    .doc(sysUserDocument)
                                    , SysUserDocument.class);
                            log.debug("response: {}", response);
                        }
                        //否则全部更新
                        else{
                            IndexResponse response = elasticsearchClient.index(request ->
                                    request.index(ElasticConstant.USER_INDEX)
                                            .id(String.valueOf(sysUserDocument.getId()))
                                            .document(sysUserDocument)
                            );
                            log.debug("response: {}", response);
                        }
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

}
