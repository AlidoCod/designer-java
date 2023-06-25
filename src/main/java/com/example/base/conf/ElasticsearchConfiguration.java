package org.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfiguration {

    static final String WEBSITE = "http://localhost:9200";
    static final String USERNAME = "elastic";
    static final String PASSWORD = "123456";

    @Bean(name = "elasticsearchClient")
    public ElasticsearchClient initClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(USERNAME, PASSWORD));
        RestClient restClient = RestClient.builder(HttpHost.create(WEBSITE))
                .setHttpClientConfigCallback(httpAsyncClientBuilder
                        -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
        RestClientTransport restClientTransport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(restClientTransport);
    }
}
