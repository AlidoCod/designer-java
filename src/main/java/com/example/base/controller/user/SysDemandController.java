package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.bean.entity.SysDemand;
import com.example.base.controller.bean.dto.demand.SysDemandInsertDto;
import com.example.base.controller.bean.dto.demand.SysDemandQueryDto;
import com.example.base.controller.bean.dto.demand.SysDemandRenewalDto;
import com.example.base.controller.bean.dto.demand.SysDemandUpdateDto;
import com.example.base.controller.bean.dto.search.ContentSearchDto;
import com.example.base.controller.bean.vo.SysDemandVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysDemandService;
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
    public Result insertDemand(@RequestBody @Valid SysDemandInsertDto appendDto) {
        sysDemandService.insert(appendDto);
        return Result.ok();
    }

    @Aggregation(path = "/renewal", method = RequestMethod.POST,
        summary = "修改需求死线", description = "与普通修改不同，注意这个会一并修改需求的状态（已过期->竞拍中, 竞拍中->竞拍中）"
    )
    public Result renewal(@RequestBody @Valid SysDemandRenewalDto renewalDto) {
        String msg = sysDemandService.renewal(renewalDto);
        return Result.ok(msg);
    }

    @Aggregation(path = "/update", method = RequestMethod.POST,
        summary = "修改需求", description = "审核"
    )
    public Result updateDemand(@RequestBody @Valid SysDemandUpdateDto updateDto) {
        sysDemandService.update(updateDto);
        return Result.ok();
    }

    @Aggregation(path = "/query", method = RequestMethod.POST,
            summary = "查询需求", description = "1. 优先查询需求ID 2.次之，查询userId 3.如果都没有，那么返回所有竞争中的需求"
    )
    public Result queryDemand(@RequestBody @Valid SysDemandQueryDto queryDto) {
        if (queryDto.getDemandId() != null) {
            SysDemandVo sysDemand = sysDemandService.queryById(queryDto.getDemandId());
            return Result.data(sysDemand);
        }
        else if (queryDto.getUserId() != null){
            List<SysDemandVo> list = sysDemandService.queryByUserId(queryDto.<SysDemand>getPage(), queryDto.getUserId());
            return Result.data(list);
        } else {
            List<SysDemandVo> list= sysDemandService.query(queryDto.<SysDemand>getPage());
            return Result.data(list);
        }
    }

    @Aggregation(path = "/delete", method = RequestMethod.GET,
        summary = "删除需求", description = "仅能删除未开始合作的需求, 因为合作中的需求肯定不能删，合作后的需求作为作品的一部分展示"
    )
    public Result deleteDemand(@RequestParam("demand-id") @NotNull Long demandId) {
        sysDemandService.delete(demandId);
        return Result.ok();
    }

    @Aggregation(path = "/search", method = RequestMethod.POST,
        summary = "搜索"
    )
    public Result search(@RequestBody @Valid ContentSearchDto searchDto) {
        List<SysDemandVo> list= sysDemandService.search(searchDto);
        return Result.data(list);
    }
}
