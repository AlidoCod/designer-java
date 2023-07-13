package com.example.base.service.user;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.base.bean.pojo.SysRecommend;
import com.example.base.client.redis.RedisZSetClient;
import com.example.base.constant.ElasticConstant;
import com.example.base.constant.GenericConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.constant.RedisSetConstant;
import com.example.base.controller.bean.vo.SysWorksVo;
import com.example.base.controller.bean.vo.base.Tuple;
import com.example.base.document.SysWorksDocument;
import com.example.base.service.plain.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysRecommendService {

    final RabbitService rabbitService;


    final RedisZSetClient zSetClient;

    @Async
    public void initFavour(Long id, String favour) {
        String[] favours = favour.split(" ");
        zSetClient.initPut(RedisSetConstant.USER_FAVOUR + id, Arrays.stream(favours).toList(), GenericConstant.FAVOUR_INIT_SCORE);
    }

    @Async
    public void updateFavour(Long id, String content) {
        String[] favours = content.split(" ");
        for (String favour : favours) {
            //降低成绩
            zSetClient.increase(RedisSetConstant.USER_FAVOUR + id, favour, GenericConstant.FAVOUR_DECREASE_STEP);
        }
    }

    @Async
    public void upload(Long userId, String tag) {
        //发送MQ消息
        rabbitService.toMsg(RabbitMQConstant.DESIGNER_RECOMMEND_EXCHANGE, RabbitMQConstant.DESIGNER_RECOMMEND_QUEUE, SysRecommend.builder().userId(userId).tag(tag).build());
    }

    /**
     * 消费消息
     */
    @RabbitListener(queues = RabbitMQConstant.DESIGNER_RECOMMEND_QUEUE)
    public void consumeMessage(Message message, Channel channel) {
        rabbitService.accept(message, channel, SysRecommend.class, sysRecommend -> {
            Long userId = sysRecommend.getUserId();
            String[] favours = sysRecommend.getTag().split(" ");
            for (String favour : favours) {
                //每次标签值加一
                zSetClient.increase(RedisSetConstant.USER_FAVOUR + userId, favour);
            }
        });
    }

    final SysWorksService sysWorksService;

    final ElasticsearchClient elasticsearchClient;

    public List<SysWorksVo> recommend(Long id) {
        List<Tuple> list = zSetClient.rangeWithScore(RedisSetConstant.USER_FAVOUR + id, 0, -1);
        //key:标签, value:权重
        //log.debug(list.toString());
        StringBuilder begin = new StringBuilder("  {\"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"should\": [");
        String end = "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String format = "        {\n" +
                "          \"match\": {\n" +
                "            \"tag\": {\n" +
                "              \"query\": \"%s\",\n" +
                "              \"boost\": %s,\n" +
                "              \"analyzer\": \"ik_max_word\"\n" +
                "            }\n" +
                "          }\n" +
                "        },";
        for (int i = 0; i < list.size(); i++) {
            Tuple tuple = list.get(i);
            if (i != list.size() - 1) {
                begin.append(String.format(format, tuple.getKey(), Double.parseDouble(tuple.getValue()) >= 0 ? tuple.getValue() : String.valueOf(0)));
            }else {
                String json = String.format(format, tuple.getKey(), Double.parseDouble(tuple.getValue()) >= 0 ? tuple.getValue() : String.valueOf(0));
                json = json.substring(0, json.length() - 1);
                begin.append(json);
            }
        }
        begin.append(end);

        //log.debug(begin.toString());

        StringReader sr = new StringReader(begin.toString());

        SearchRequest request = new SearchRequest.Builder()
                .index(ElasticConstant.WORKS_INDEX)
                .withJson(sr)
                .build();

        Set<Long> ids = new HashSet<>();

        try {
            SearchResponse<SysWorksDocument> response = elasticsearchClient.search(request, SysWorksDocument.class);
            for (Hit<SysWorksDocument> hit : response.hits().hits()) {
                SysWorksDocument source = hit.source();
                assert source != null;
                ids.add(source.getId());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        for (Tuple tuple : list) {
//             Query matchQuery = MatchPhraseQuery.of(m ->
//                        m.query(tuple.getKey())
//                                .field("tag")
//                                .analyzer("ik_max_word")
//                                //修改权重
//                                .boost(Float.valueOf(tuple.getValue()))
//                    )._toQuery();
//             queries.add(matchQuery);
//        }
//        Query bool = BoolQuery.of(m ->
//                    m.should(queries)
//                )._toQuery();
//        List<Long> ids = new ArrayList<>();
//        try {
//            if (!queries.isEmpty()) {
//                SearchResponse<SysWorksDocument> response = elasticsearchClient.search(request ->
//                                request.query(bool)
//                        , SysWorksDocument.class);
//                for (Hit<SysWorksDocument> hit : response.hits().hits()) {
//                    SysWorksDocument source = hit.source();
//                    assert source != null;
//                    ids.add(source.getId());
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        //通过排行榜获取id
        //随机获取一半的非推荐id
        Set<String> randomIds = zSetClient.range(RedisSetConstant.RANK_WORKS, 0, -1);
        int limit = ids.size() / 2;
        //随机数种子
        Random random = new Random(System.currentTimeMillis());
        for (String randomId : randomIds) {
            if (limit == 0) {
                break;
            }
            double possible = random.nextDouble(1);
            //有一半的概率添加随机数据
            if (possible > 0.5) {
                ids.add(Long.valueOf(randomId));
            }
        }
        List<SysWorksVo> recommendList = new ArrayList<>(ids.stream().map(sysWorksService::queryById).toList());
        Collections.shuffle(recommendList, random);
        return recommendList;
    }
}
