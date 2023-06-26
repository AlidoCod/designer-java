package com.example.base.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.entity.ExamineDemand;
import com.example.base.bean.entity.SysDemand;
import com.example.base.controller.bean.dto.base.BasePage;
import com.example.base.controller.bean.dto.demand.RejectDemandDto;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.admin.ManagerSysDemandService;
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
@Tag(name = "管理需求")
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/demand")
@Controller
public class ManagerSysDemandController {

    final ManagerSysDemandService managerSysDemandService;

    @Aggregation(path = "/query", method = RequestMethod.POST,
        summary = "查询需要审核的需求"
    )
    public Result query(@RequestBody BasePage basePage) {
        Page<ExamineDemand> query = managerSysDemandService.query(basePage);
        return Result.data(query);
    }

    @Aggregation(path = "/accept", method = RequestMethod.GET,
            summary = "审核通过"
    )
    public Result accept(@RequestParam("id") Long id) {
        managerSysDemandService.accept(id);
        return Result.ok();
    }

    @Aggregation(path = "/reject", method = RequestMethod.POST,
        summary = "审核失败"
    )
    public Result reject(@RequestBody RejectDemandDto rejectDemandDto) {
        managerSysDemandService.reject(rejectDemandDto);
        return Result.ok();
    }
}
