package com.example.base.service.user;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.ExamineUser;
import com.example.base.bean.entity.SysUser;
import com.example.base.bean.entity.enums.ExamineCondition;
import com.example.base.bean.entity.enums.UserStatus;
import com.example.base.constant.ElasticConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.search.ContentSearchDto;
import com.example.base.controller.bean.dto.user.SysUserUpdateDto;
import com.example.base.controller.bean.vo.SysUserVo;
import com.example.base.document.SysUserDocument;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.ExamineUserRepository;
import com.example.base.repository.SysUserRepository;
import com.example.base.service.plain.SysResourceService;
import com.example.base.utils.BeanCopyUtil;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

    final SysUserRepository userRepository;
    final ExamineUserRepository examineUserRepository;

    public void update(Long id, SysUserUpdateDto updateDto) {
        ExamineUser copy = BeanCopyUtil.copy(updateDto, ExamineUser.class);
        //设置审核状态
        copy.setExamineCondition(ExamineCondition.ING);
        copy.setId(null);
        copy.setCheatId(id);
        examineUserRepository.insert(copy);
    }

    public SysUserVo queryById(Long id) {
        SysUserService sysUserService = beanFactory.getBean(SysUserService.class);
        SysUser sysUser = sysUserService.query(id);
        return SysUserVo.getInstance(beanFactory, sysUser);
    }

    @Cacheable(value = RedisConstant.CACHE_USER_ID, key = "#root.args[0]")
    public SysUser query(Long id) {
        return userRepository.selectById(id);
    }

    final ElasticsearchClient elasticsearchClient;

    final BeanFactory beanFactory;

    final SysResourceService sysResourceService;
    public List<SysUserVo> search(ContentSearchDto searchDto) {
        String content = searchDto.getContent();
        Page<SysUserDocument> page = searchDto.getPage();
        //昵称采用模糊查询
        Query byNickname = MatchQuery.of(m ->
                m.field("nickname")
                        .query(content)
                        .analyzer("ik_max_word")
                        .boost(3.0F)
        )._toQuery();
        //前缀匹配查询
        Query byNicknamePrefix = MatchPhrasePrefixQuery.of(m ->
                    m.field("nickname")
                            .query(content)
                            .analyzer("ik_max_word")
                            .boost(4.0F)
                )._toQuery();
        //username采用精确查询
        Query byUsername = TermQuery.of(m ->
                m.field("username")
                        .value(content)
                        .boost(5.0F)
        )._toQuery();

        //email精确查询
        Query byEmail = TermQuery.of(m ->
                m.field("email")
                        .value(content)
                        .boost(5.0F)
        )._toQuery();
        Query byTag = MatchQuery.of(m ->
                m.field("tag")
                        .query(content)
                        .analyzer("ik_max_word")
                        .boost(3.0F)
        )._toQuery();
        Query byTagPrefix = MatchPhrasePrefixQuery.of(m ->
                    m.field("tag")
                            .query(content)
                            .analyzer("ik_max_word")
                            .boost(4.0F)
                )._toQuery();
        Query boolQuery = BoolQuery.of(m ->
                m.should(List.of(byUsername,
                        byEmail,
                        byNickname,
                        byTag,
                        byNicknamePrefix,
                        byTagPrefix))
        )._toQuery();
        try {
            SearchResponse<SysUserDocument> response = elasticsearchClient.search(request ->
                            request.index(ElasticConstant.USER_INDEX)
                                    .query(boolQuery)
                                    .from((int) ((page.getCurrent() - 1) * page.getSize()))
                                    .size((int) page.getSize())
                    , SysUserDocument.class);
            SysUserService bean = beanFactory.getBean(SysUserService.class);
            return response.hits().hits().stream()
                    .map(o -> {
                        SysUserDocument source = o.source();
                        return bean.queryById(source.getId());
                    }).toList();
        } catch (IOException e) {
            log.error("", e);
            throw GlobalRuntimeException.of("用户搜索异常");
        }
    }

    public Integer status(Long id) {
        Channel channel = UserConnectPool.getChannel(id);
        return channel == null ? UserStatus.OFFLINE : UserStatus.ONLINE;
    }
}
