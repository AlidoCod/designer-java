package com.example.base.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.entity.SysCompeteDemand;
import com.example.base.controller.bean.dto.compete_demand.SysCompeteDemandInsertDto;
import com.example.base.controller.bean.dto.compete_demand.SysCompeteDemandQueryDto;
import com.example.base.controller.bean.dto.compete_demand.SysCompeteDemandUpdateDto;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysCompeteDemandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Tag(name = "竞拍", description = "类似于闲鱼，我想要，但是并不决定最终价格")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/compete-demand")
@Controller
public class SysCompeteDemandController {

    final SysCompeteDemandService sysCompeteDemandService;

    @Aggregation(path = "/is-exist", method = RequestMethod.GET,
        summary = "查看当前需求竞拍是否已存在", description = "一个用户只能对同一个需求竞拍一次"
    )
    public Result isExist(@RequestParam("demandId")Long demandId, @RequestParam("userId")Long userId) {
        Boolean exist = sysCompeteDemandService.isExist(demandId, userId);
        return Result.ok(String.valueOf(exist));
    }

    @Aggregation(path = "/insert", method = RequestMethod.POST,
        summary = "新增竞拍", description = "请求前必须校验is-exist，后端降低接口耦合度未作防御式编程"
    )
    public Result insert(@RequestBody SysCompeteDemandInsertDto insertDto) {
        sysCompeteDemandService.insert(insertDto);
        return Result.ok();
    }

    @Aggregation(path = "/query", method = RequestMethod.POST,
        summary = "查询竞拍", description = "传demandId则获取需求竞拍，否则获取个人竞拍;"
    )
    public Result query(@RequestBody SysCompeteDemandQueryDto queryDto) {
        Page<SysCompeteDemand> page;
        if (queryDto.getDemandId() != null) {
            page = sysCompeteDemandService.queryByDemandId(queryDto.getPage(), queryDto.getDemandId());
        }
        else {
            page = sysCompeteDemandService.queryByUserId(queryDto.getPage(), queryDto.getUserId());
        }
        return Result.data(page);
    }

    @Aggregation(path = "/update", method = RequestMethod.POST,
        summary = "更新竞拍", description = "只能修改描述和价格"
    )
    public Result update(@RequestBody SysCompeteDemandUpdateDto updateDto) {
        sysCompeteDemandService.update(updateDto);
        return Result.ok();
    }
}
