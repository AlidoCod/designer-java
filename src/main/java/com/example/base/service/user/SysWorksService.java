package com.example.base.service.user;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhrasePrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.SysWorks;
import com.example.base.client.redis.RedisZSetClient;
import com.example.base.constant.ElasticConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.constant.RedisSetConstant;
import com.example.base.controller.bean.dto.works.SysWorksUpdateDto;
import com.example.base.controller.bean.dto.works.SysWorksUploadDto;
import com.example.base.controller.bean.vo.SysWorksVo;
import com.example.base.document.SysWorksDocument;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.SysCooperateDemandRepository;
import com.example.base.repository.SysWorksRepository;
import com.example.base.service.plain.RabbitService;
import com.example.base.utils.BeanCopyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysWorksService {

    final SysWorksRepository sysWorksRepository;
    final RedisZSetClient zSetClient;
    final RabbitService rabbitService;
    final SysCooperateDemandRepository sysCooperateDemandRepository;

    @Transactional
    public Long upload(SysWorksUploadDto uploadDto) {
        SysWorks copy = BeanCopyUtil.copy(uploadDto, SysWorks.class);
        int insert = sysWorksRepository.insert(copy);
        //维护收藏量
        zSetClient.put(RedisSetConstant.RANK_WORKS, String.valueOf(copy.getId()));
        if (insert == 1) {
            //发送消息更新ES
            rabbitService.toMsg(RabbitMQConstant.DESIGNER_INSERT_EXCHANGE, RabbitMQConstant.WORKS_INSERT_QUEUE, copy);
            return copy.getId();
        }
        throw GlobalRuntimeException.of("作品上传失败");
    }

    public SysWorksVo queryById(Long id) {
        SysWorksService bean = beanFactory.getBean(SysWorksService.class);
        SysWorks sysWorks = bean.query(id);
        //若存在需求ID代表隐私作品
        if (sysWorks.getDemandId() != null) {
            throw GlobalRuntimeException.of("意料之外的查询，隐私作品无法查看");
        }
        return SysWorksVo.getInstance(beanFactory, sysWorks);
    }

    //缓存重构
    @Cacheable(value = RedisConstant.CACHE_WORKS_ID, key = "#root.args[0]")
    public SysWorks query(Long id) {
        return sysWorksRepository.selectById(id);
    }

    final BeanFactory beanFactory;

    public List<SysWorksVo> queryByDesignerId(Page<SysWorks> page, Long designerId) {
        page = sysWorksRepository.selectPage(page,
                Wrappers.<SysWorks>lambdaQuery()
                        .eq(SysWorks::getDesignerId, designerId)
                        .isNull(SysWorks::getDemandId)
                        .select(SysWorks::getId)
        );
        List<SysWorksVo> list = new ArrayList<>();
        SysWorksService bean = beanFactory.getBean(SysWorksService.class);
        page.getRecords().forEach(o -> {
            SysWorksVo tmp = bean.queryById(o.getId());
            list.add(tmp);
        });
        return list;
    }

    @Transactional
    @CacheEvict(value = RedisConstant.CACHE_WORKS_ID, key = "#updateDto.id")
    public void update(SysWorksUpdateDto updateDto) {
        SysWorks copy = BeanCopyUtil.copy(updateDto, SysWorks.class);
        sysWorksRepository.updateById(copy);
        //发送到ES
        rabbitService.toMsg(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE, RabbitMQConstant.WORKS_UPDATE_QUEUE, copy);
    }

    //若抛出异常，则不清除缓存
    @Transactional
    @CacheEvict(value = RedisConstant.CACHE_WORKS_ID, key = "#root.args[0]", beforeInvocation = true)
    public void delete(Long id) {
        SysWorks sysWorks = sysWorksRepository.selectById(id);
        if (sysWorks != null && sysWorks.getDemandId() == null) {
            sysWorksRepository.deleteById(id);
            rabbitService.toMsg(RabbitMQConstant.DESIGNER_DELETE_EXCHANGE, RabbitMQConstant.WORKS_DELETE_QUEUE, sysWorks);
        }
        throw GlobalRuntimeException.of("作品已绑定需求，无法删除");
    }

    final ElasticsearchClient elasticsearchClient;

    //理论上es是查询不到需求作品的
    public List<SysWorksVo> search(Page<SysWorksDocument> page, String content) {
        Query byTitle = matchQuery("title", content, 3.0f);
        Query byTheme = matchQuery("theme", content, 3.0f);
        Query byBody = matchQuery("body", content, 2.0f);
        Query byTag = matchQuery("tag", content, 3.5f);

        Query byTitlePrefix = matchPrefixQuery("title", content, 4.0f);
        Query byThemePrefix = matchPrefixQuery("theme", content, 4.0f);
        Query byBodyPrefix = matchPrefixQuery("body", content, 2.5f);
        Query byTagPrefix = matchPrefixQuery("tag", content, 4.5f);

        Query bool = BoolQuery.of(m ->
                m.should(List.of(byTitle,
                        byTheme,
                        byBody,
                        byTag,
                        byTitlePrefix,
                        byThemePrefix,
                        byBodyPrefix,
                        byTagPrefix
                        ))
        )._toQuery();
        try {
            SearchResponse<SysWorksDocument> response = elasticsearchClient.search(request ->
                            request.query(bool)
                                    .index(ElasticConstant.WORKS_INDEX)
                                    .from((int) ((page.getCurrent() - 1) * page.getSize()))
                                    .size((int) page.getSize())
                    , SysWorksDocument.class);
            List<SysWorksVo> list = new ArrayList<>();
            for (Hit<SysWorksDocument> hit : response.hits().hits()) {
                SysWorksDocument source = hit.source();
                SysWorksService bean = beanFactory.getBean(SysWorksService.class);
                SysWorksVo sysWorksVo = bean.queryById(source.getId());
                if (sysWorksVo.getDemandId() == null) {
                    list.add(sysWorksVo);
                }
            }
            return list;
        } catch (IOException e) {
            log.error("", e);
            throw GlobalRuntimeException.of("作品搜索异常");
        }
    }

    private Query matchQuery(String field, String content, float boost) {
        return MatchQuery.of(m ->
                m.field(field)
                        .analyzer("ik_smart")
                        .query(content)
                        .boost(boost)
        )._toQuery();
    }

    private Query matchPrefixQuery(String field, String content, float boost) {
        return MatchPhrasePrefixQuery.of(m ->
                    m.field(field)
                            .analyzer("ik_smart")
                            .query(content)
                            .boost(boost)
                )._toQuery();
    }
}
