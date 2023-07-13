package com.example.base.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.example.base.constant.ElasticConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.document.SysWorksDocument;
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
public class WorksQueueListener {

    final RabbitService rabbitService;
    final ElasticsearchClient elasticsearchClient;

    @RabbitListener(queues = RabbitMQConstant.WORKS_INSERT_QUEUE)
    public void insertMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysWorksDocument.class, sysWorksDocument ->
                {
                    try {
                        IndexResponse response = elasticsearchClient.index(request ->
                                request.index(ElasticConstant.WORKS_INDEX)
                                        .id(String.valueOf(sysWorksDocument.getId()))
                                        .document(sysWorksDocument)
                        );
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstant.WORKS_DELETE_QUEUE)
    public void deleteMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysWorksDocument.class, sysWorksDocument ->
                {
                    try {
                        DeleteResponse response = elasticsearchClient.delete(request ->
                                request.index(ElasticConstant.WORKS_INDEX)
                                        .id(String.valueOf(sysWorksDocument.getId()))
                        );
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstant.WORKS_UPDATE_QUEUE)
    public void updateMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysWorksDocument.class, sysWorksDocument ->
                {
                    try {
                        UpdateResponse<SysWorksDocument> response = elasticsearchClient.update(request ->
                                        request.index(ElasticConstant.WORKS_INDEX)
                                                .id(String.valueOf(sysWorksDocument.getId()))
                                                .doc(sysWorksDocument)
                                , SysWorksDocument.class);
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

}
