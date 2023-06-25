package com.example.base;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@Slf4j
@SpringBootTest
public class ElasticClientTest {

    @Autowired
    ElasticsearchClient client;

    @Test
    public void functionTest() throws IOException {
        GetIndexResponse response = client.indices().get(request ->
                request.index("user"));
        Map<String, Property> properties = response.get("user").mappings().properties();
        properties.forEach((k, v) -> {
            log.info("k: {}, v: {}", k, v);
        });
    }
}
