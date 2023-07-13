package com.example.base.service.user;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.ExamineDemand;
import com.example.base.bean.entity.SysDemand;
import com.example.base.bean.entity.enums.DemandCondition;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.constant.ElasticConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.demand.SysDemandInsertDto;
import com.example.base.controller.bean.dto.demand.SysDemandRenewalDto;
import com.example.base.controller.bean.dto.demand.SysDemandUpdateDto;
import com.example.base.controller.bean.dto.search.ContentSearchDto;
import com.example.base.controller.bean.vo.SysDemandVo;
import com.example.base.document.SysDemandDocument;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.ExamineDemandRepository;
import com.example.base.repository.SysDemandRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysDemandService {

    final SysDemandRepository sysDemandRepository;
    final ExamineDemandRepository examineDemandRepository;
    final RabbitService rabbitService;

    public void insert(SysDemandInsertDto insertDto) {
        ExamineDemand copy = BeanCopyUtil.copy(insertDto, ExamineDemand.class);
        copy.setId(null);
        copy.setExamineCondition(ExamineCondition.ING);
        examineDemandRepository.insert(copy);
    }

    public void update(SysDemandUpdateDto updateDto) {
        SysDemand sysDemand = sysDemandRepository.selectById(updateDto.getId());
        if (!sysDemand.getDemandCondition().equals(DemandCondition.IN_BIDDING)){
            throw GlobalRuntimeException.of("需求非竞拍状态，无法更新");
        }
        ExamineDemand copy = BeanCopyUtil.copy(updateDto, ExamineDemand.class);
        copy.setId(null);
        //区分新增和更新
        copy.setCheatId(updateDto.getId());
        copy.setExamineCondition(ExamineCondition.ING);
        //设置死线、userId，避免前端为空
        copy.setDeadTime(sysDemand.getDeadTime());
        copy.setUserId(sysDemand.getUserId());

        examineDemandRepository.insert(copy);
    }

    public SysDemandVo queryById(Long demandId) {
        SysDemandService bean = beanFactory.getBean(SysDemandService.class);
        SysDemand sysDemand = bean.query(demandId);
        return SysDemandVo.getInstance(beanFactory, sysDemand);
    }

    @Cacheable(value = RedisConstant.CACHE_DEMAND_ID, key = "#root.args[0]")
    public SysDemand query(Long id) {
        return sysDemandRepository.selectById(id);
    }

    final BeanFactory beanFactory;

    public List<SysDemandVo> queryByUserId(Page<SysDemand> page, Long userId) {
        page = sysDemandRepository.selectPage(page,
                Wrappers.<SysDemand>lambdaQuery()
                        .eq(SysDemand::getUserId, userId)
                        .select(SysDemand::getId)
        );
        List<SysDemandVo> record = new ArrayList<>();
        SysDemandService bean = beanFactory.getBean(SysDemandService.class);
        //通过缓存查数据
        page.getRecords().forEach(o -> {
            SysDemandVo tmp = bean.queryById(o.getId());
            record.add(tmp);
        });
        return record;
    }

    @Transactional
    @CacheEvict(value = RedisConstant.CACHE_DEMAND_ID, key = "#root.args[0]")
    public void delete(Long demandId) {
        SysDemand sysDemand = sysDemandRepository.selectById(demandId);
        if (sysDemand == null) {
            throw GlobalRuntimeException.of("未找到对应的需求，无法删除!");
        }
        if (Objects.equals(sysDemand.getDemandCondition(), DemandCondition.IN_BIDDING) || Objects.equals(sysDemand.getDemandCondition(), DemandCondition.DEAD)) {
            throw GlobalRuntimeException.of("已合作的需求，无法删除!");
        }
        sysDemandRepository.deleteById(demandId);
        //发送消息删除ES
        rabbitService.toMsg(RabbitMQConstant.DESIGNER_DELETE_EXCHANGE, RabbitMQConstant.DEMAND_DELETE_QUEUE, sysDemand);
    }

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");

    @Transactional
    public String renewal(SysDemandRenewalDto renewalDto) {
        String msg;
        SysDemand sysDemand = sysDemandRepository.selectById(renewalDto.getId());
        SysDemand copy = BeanCopyUtil.copy(renewalDto, SysDemand.class);
        //0,3状态，正常执行，否则抛出异常
        if (sysDemand.getDemandCondition().equals(DemandCondition.DEAD) || sysDemand.getDemandCondition().equals(DemandCondition.IN_BIDDING)) {
            copy.setDemandCondition(DemandCondition.IN_BIDDING);
        }
        else {
            throw GlobalRuntimeException.of("竞争中或已完成的需求无法修改死线，即无法续期");
        }
        sysDemandRepository.updateById(copy);
        //更新ES，避免状态错误
        rabbitService.toMsg(RabbitMQConstant.DEMAND_UPDATE_QUEUE, RabbitMQConstant.DEMAND_UPDATE_QUEUE, sysDemand);
        msg = "修改死线成功，原期限: " + sysDemand.getDeadTime().format(formatter) + " -> " + renewalDto.getDeadTime().format(formatter);
        return msg;
    }

    public List<SysDemandVo> query(Page<SysDemand> page) {
        page = sysDemandRepository.selectPage(page,
                Wrappers.<SysDemand>lambdaQuery()
                        .eq(SysDemand::getDemandCondition, DemandCondition.IN_BIDDING)
                        .select(SysDemand::getId)
        );
        SysDemandService bean = beanFactory.getBean(SysDemandService.class);
        List<SysDemandVo> list = new ArrayList<>();
        page.getRecords().forEach(o -> {
            SysDemandVo tmp = bean.queryById(o.getId());
            list.add(tmp);
        });
        return list;
    }

    final ElasticsearchClient elasticsearchClient;

    public List<SysDemandVo> search(ContentSearchDto searchDto) {
        String content = searchDto.getContent();
        Page<SysDemandDocument> page = searchDto.getPage();

        Query byTitle = MatchQuery.of(m ->
                    m.field("title")
                            .analyzer("ik_smart")
                            .query(content)
                            .boost(3.0F)
                )._toQuery();

        Query byTitlePrefix = MatchPhrasePrefixQuery.of(m ->
                    m.field("title")
                            .analyzer("ik_smart")
                            .query(content)
                            .boost(4.0F)
                )._toQuery();
        Query byTheme = MatchQuery.of(m ->
                m.field("theme")
                        .analyzer("ik_smart")
                        .query(content)
        )._toQuery();
        Query byBody = MatchQuery.of(m ->
                m.field("body")
                        .analyzer("ik_smart")
                        .query(content)
                        .boost(2.0F)
        )._toQuery();
        Query byBodyPrefix = MatchPhrasePrefixQuery.of(m ->
                    m.field("body")
                            .query("ik_smart")
                            .query(content)
                            .boost(3.0f)
                )._toQuery();
        Query byTechnicalSelection = MatchQuery.of(m ->
                m.field("technicalSelection")
                        .analyzer("ik_smart")
                        .query(content)
                        .boost(4.0F)
        )._toQuery();
        Query byTechnicalSelectionPrefix = MatchPhrasePrefixQuery.of(m ->
                    m.field("technicalSelection")
                            .analyzer("ik_smart")
                            .query(content)
                            .boost(5.0f)
                )._toQuery();
        Query byDemandCondition = TermQuery.of(m ->
                m.field("demandCondition")
                        .value(DemandCondition.IN_BIDDING)
        )._toQuery();
        Query bool = BoolQuery.of(m ->
                    m.should(List.of(byTitle,
                                    byTheme,
                                    byBody,
                                    byTitlePrefix,
                                    byTechnicalSelectionPrefix,
                                    byBodyPrefix,
                                    byTechnicalSelection))
                            .filter(byDemandCondition)
                )._toQuery();
        try {
            SearchResponse<SysDemandDocument> response = elasticsearchClient.search(request ->
                            request.query(bool)
                                    .index(ElasticConstant.DEMAND_INDEX)
                                    .from((int) ((page.getCurrent() - 1) * page.getSize()))
                                    .size((int) page.getSize())
                    , SysDemandDocument.class);

            return response.hits().hits()
                    .stream()
                    .map(o -> {
                        SysDemandDocument source = o.source();
                        //log.debug(source.toString());
                        SysDemandService sysDemandService = beanFactory.getBean(SysDemandService.class);
                        return sysDemandService.queryById(source.getId());
                    }).toList();
        } catch (IOException e) {
            log.error("", e);
            throw GlobalRuntimeException.of("需求搜索异常");
        }
    }
}
