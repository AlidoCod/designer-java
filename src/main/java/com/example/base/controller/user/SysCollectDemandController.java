package com.example.base.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.entity.SysCollectDemand;
import com.example.base.controller.bean.dto.collect_demand.SysCollectDemandInsertDto;
import com.example.base.controller.bean.dto.collect_demand.SysCollectDemandQueryDto;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysCollectDemandService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public Result isCollect(@RequestBody SysCollectDemandInsertDto isCollectDto) {
           Boolean flag = sysCollectDemandService.isCollect(isCollectDto);
           return Result.ok(String.valueOf(flag));
    }

    @Aggregation(path = "/query", method = RequestMethod.POST,
        summary = "查看用户收藏"
    )
    public Result query(@RequestBody SysCollectDemandQueryDto queryDto) {
        Page<SysCollectDemand> page = sysCollectDemandService.query(queryDto.<SysCollectDemand>getPage(), queryDto.getUserId());
        return Result.data(page);
    }

    @Aggregation(path = "/insert", method = RequestMethod.POST,
        summary = "添加收藏", description = "添加前请调用is-collect确保不重复收藏，为效率和低耦合未作防御式编程"
    )
    public Result insert(@RequestBody SysCollectDemandInsertDto insertDto) {
        sysCollectDemandService.insert(insertDto);
        return Result.ok();
    }

    @Aggregation(path = "/delete", method = RequestMethod.POST,
        summary = "取消收藏"
    )
    public Result delete(@RequestBody SysCollectDemandInsertDto deleteDto) {
        sysCollectDemandService.delete(deleteDto);
        return Result.ok();
    }
}
