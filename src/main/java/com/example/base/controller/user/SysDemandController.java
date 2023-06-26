package com.example.base.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.entity.SysDemand;
import com.example.base.controller.bean.dto.demand.SysDemandInsertDto;
import com.example.base.controller.bean.dto.demand.SysDemandQueryDto;
import com.example.base.controller.bean.dto.demand.SysDemandUpdateDto;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysDemandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Tag(name = "用户需求")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/demand")
@Controller
public class SysDemandController {

    final SysDemandService sysDemandService;

    @Aggregation(path = "/insert", method = RequestMethod.POST,
        summary = "添加需求", description = "审核"
    )
    public Result insertDemand(@RequestBody SysDemandInsertDto appendDto) {
        sysDemandService.insertDemand(appendDto);
        return Result.ok();
    }

    @Aggregation(path = "/update", method = RequestMethod.POST,
        summary = "修改需求", description = "审核"
    )
    public Result updateDemand(@RequestBody @Valid SysDemandUpdateDto updateDto) {
        sysDemandService.updateDemand(updateDto);
        return Result.ok();
    }

    @Aggregation(path = "/query", method = RequestMethod.POST,
            summary = "查询需求", description = "1. 优先查询需求ID 2.次之，查询userId"
    )
    public Result queryDemand(@RequestBody SysDemandQueryDto queryDto) {
        if (queryDto.getDemandId() != null) {
            SysDemand sysDemand = sysDemandService.queryDemandByDemandId(queryDto.getDemandId());
            return Result.data(sysDemand);
        }
        else {
            Page<SysDemand> sysDemandPage = sysDemandService.queryDemandByUserId(queryDto.<SysDemand>getPage(), queryDto.getUserId());
            return Result.data(sysDemandPage);
        }
    }

    @Aggregation(path = "/delete", method = RequestMethod.GET,
        summary = "删除需求", description = "仅能删除未开始合作的需求, 因为合作中的需求肯定不能删，合作后的需求作为作品的一部分展示"
    )
    public Result deleteDemand(@RequestParam("demand-id")Long demandId) {
        sysDemandService.deleteDemand(demandId);
        return Result.ok();
    }
}
