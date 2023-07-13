package com.example.base.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.pojo.SysCollectDemand;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.collect_demand.SysCollectDemandInsertDto;
import com.example.base.controller.bean.dto.collect_demand.SysCollectDemandQueryDto;
import com.example.base.controller.bean.vo.SysDemandVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.controller.bean.vo.base.Tuple;
import com.example.base.service.user.SysCollectDemandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Tag(name = "收藏需求")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/collect-demand")
@Controller
public class SysCollectDemandController {

    final SysCollectDemandService sysCollectDemandService;

    @Aggregation(path = "/is-collect", method = RequestMethod.POST,
        summary = "当前需求是否已收藏"
    )
    public Result isCollect(@RequestBody @Valid SysCollectDemandInsertDto isCollectDto) {
           Boolean flag = sysCollectDemandService.isCollect(isCollectDto);
           return Result.data(String.valueOf(flag));
    }

    @Aggregation(path = "/query", method = RequestMethod.POST,
        summary = "查看用户收藏"
    )
    public Result query(@RequestBody @Valid SysCollectDemandQueryDto queryDto) {
        List<SysDemandVo> list = sysCollectDemandService.query(queryDto.<SysCollectDemand>getPage(), queryDto.getUserId());
        return Result.data(list);
    }

    @Aggregation(path = "/insert", method = RequestMethod.POST,
        summary = "添加收藏", description = "添加前请调用is-collect确保不重复收藏，为效率和低耦合未作防御式编程"
    )
    public Result insert(@RequestBody @Valid SysCollectDemandInsertDto insertDto) {
        sysCollectDemandService.insert(insertDto);
        return Result.ok();
    }

    @Aggregation(path = "/delete", method = RequestMethod.POST,
        summary = "取消收藏"
    )
    public Result delete(@RequestBody @Valid SysCollectDemandInsertDto deleteDto) {
        sysCollectDemandService.delete(deleteDto);
        return Result.ok();
    }

    @Aggregation(path = "/count", method = RequestMethod.GET,
        summary = "计算需求收藏量"
    )
    public Result count(@RequestParam("demand-id") @NotNull String demandId) {
        Double count = sysCollectDemandService.count(demandId);
        return Result.data(count);
    }

    @Aggregation(path = "/rank", method = RequestMethod.GET,
        summary = "获取当前排名"
    )
    public Result rank(@RequestParam("demand-id") @NotNull String demandId) {
        Long rank = sysCollectDemandService.rank(demandId);
        return Result.data(rank);
    }

    @Aggregation(path = "/ranks", method = RequestMethod.POST,
        summary = "需求收藏排行榜"
    )
    public Result rank(@RequestBody @Valid BasePage page) {
        Page<Tuple> sysCollectDemandPage = sysCollectDemandService.ranks(page.<Tuple>getPage());
        return Result.data(sysCollectDemandPage.getRecords());
    }
}
