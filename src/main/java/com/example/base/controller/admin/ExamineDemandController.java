package com.example.base.controller.admin;

import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.demand.RejectDemandDto;
import com.example.base.controller.bean.vo.ExamineDemandVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.admin.ExamineDemandService;
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
@Tag(name = "管理需求")
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/demand")
@Controller
public class ExamineDemandController {

    final ExamineDemandService examineDemandService;

    @Aggregation(path = "/query", method = RequestMethod.POST,
        summary = "查询需要审核的需求"
    )
    public Result query(@Valid @RequestBody BasePage basePage) {
        List<ExamineDemandVo> query = examineDemandService.query(basePage);
        //query.getRecords().forEach(o -> log.debug(o.getId().toString()));
        return Result.data(query);
    }

    @Aggregation(path = "/accept", method = RequestMethod.GET,
            summary = "审核通过"
    )
    public Result accept(@NotNull @RequestParam("id") Long id) {
        examineDemandService.accept(id);
        return Result.ok();
    }

    @Aggregation(path = "/reject", method = RequestMethod.POST,
        summary = "审核失败"
    )
    public Result reject(@Valid @RequestBody RejectDemandDto rejectDemandDto) {
        examineDemandService.reject(rejectDemandDto);
        return Result.ok();
    }
}
