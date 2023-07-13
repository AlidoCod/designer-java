package com.example.base.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.example.base.constant.ElasticConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.document.SysDemandDocument;
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
public class DemandQueueListener {

    final RabbitService rabbitService;
    final ElasticsearchClient elasticsearchClient;

    @RabbitListener(queues = RabbitMQConstant.DEMAND_INSERT_QUEUE)
    public void insertMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysDemandDocument.class, sysDemandDocument ->
                {
                    try {
                        IndexResponse response = elasticsearchClient.index(request ->
                                request.index(ElasticConstant.DEMAND_INDEX)
                                        .id(String.valueOf(sysDemandDocument.getId()))
                                        .document(sysDemandDocument)
                        );
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstant.DEMAND_DELETE_QUEUE)
    public void deleteMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysDemandDocument.class, sysDemandDocument ->
                {
                    try {
                        DeleteResponse response = elasticsearchClient.delete(request ->
                                request.index(ElasticConstant.DEMAND_INDEX)
                                        .id(String.valueOf(sysDemandDocument.getId()))
                        );
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstant.DEMAND_UPDATE_QUEUE)
    public void updateMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysDemandDocument.class, sysDemandDocument ->
                {
                    try {
                        UpdateResponse<SysDemandDocument> response = elasticsearchClient.update(request ->
                                        request.index(ElasticConstant.DEMAND_INDEX)
                                                .id(String.valueOf(sysDemandDocument.getId()))
                                                .doc(sysDemandDocument)
                                , SysDemandDocument.class);
                        log.debug("response: {}", response);
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
        );
    }

}
