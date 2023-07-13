package com.example.base.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.annotation.Aggregation;
import com.example.base.bean.entity.SysCooperateDemand;
import com.example.base.controller.bean.dto.cooperate_demand.*;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysCooperateDemandService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Tag(name = "需求-合作")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/cooperate-demand")
@Controller
public class SysCooperateDemandController {

    final SysCooperateDemandService sysCooperateDemandService;

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Aggregation(path = "/insert", method = RequestMethod.POST,
        summary = "提交合作表单"
    )
    public Result insert(@RequestBody @Valid SysCooperateDemandInsertDto insertDto) {
        insertDto.setDeadTime(LocalDateTime.parse(insertDto.getTempTime(), formatter));
        sysCooperateDemandService.insert(insertDto);
        return Result.ok();
    }

    @Aggregation(path = "/confirm", method = RequestMethod.POST,
        summary = "设计师确认", description = "祝前端好运"
    )
    public Result confirm(@RequestBody @Valid SysCooperateDemandConfirmDto cooperateDemandConfirmDto) {
        Boolean flag = sysCooperateDemandService.confirm(cooperateDemandConfirmDto);
        return Result.data(flag);
    }

    @Aggregation(path = "/is-confirm", method = RequestMethod.GET,
        summary = "查询设计师是否已确认"
    )
    public Result isConfirm(@RequestParam("demandId") Long demandId,
                            @RequestParam("designerId") Long designerId) {
        Boolean confirm = sysCooperateDemandService.isConfirm(demandId, designerId);
        return Result.data(confirm);
    }

    @Aggregation(path = "/is-cooperate", method = RequestMethod.GET,
        summary = "查询设计师是否与该需求（用户）合作成功", description = "true: 代表已合作, false: 代表未合作"
    )
    public Result isCooperate(@RequestParam("demandId") Long demandId,
                              @RequestParam("designerId") Long designerId
                              ) {
        Boolean isCooperate = sysCooperateDemandService.isCooperate(demandId, designerId);
        return Result.data(isCooperate);
    }

    @Aggregation(path = "/query-cooperate", method = RequestMethod.GET,
        summary = "设计师查询合作"
    )
    public Result queryCooperate(@RequestParam("demandId") Long demandId,
                                 @RequestParam("designerId") Long designerId
                                 ) {
        SysCooperateDemand sysCooperateDemand = sysCooperateDemandService.queryCooperate(demandId, designerId);
        return Result.data(sysCooperateDemand);
    }

    @Aggregation(path = "/query/user-cooperate", method = RequestMethod.GET,
        summary = "用户查询合作"
    )
    public Result queryUserCooperation(@RequestParam("demandId") Long demandId,
                                       @RequestParam("userId") Long userId
                                       ){
        SysCooperateDemand sysCooperateDemand = sysCooperateDemandService.queryUserCooperation(demandId, userId);
        return Result.data(sysCooperateDemand);
    }


//    @Aggregation(path = "/check-pay", method = RequestMethod.GET,
//            summary = "检查是否支付剩余金额", description = "请使用二维码支付，此接口用于更新支付状态"
//    )
//    public Result checkPay(@RequestParam("id") @NotNull Long id) {
//        sysCooperateDemandService.checkPay(id);
//        return Result.ok();
//    }

    @Aggregation(path = "/upload", method = RequestMethod.POST,
        summary = "上传作品", description = "上传绑定需求的作品"
    )
    public Result upload(@RequestBody SysCooperateDemandWorksUploadDto uploadDto) {
        sysCooperateDemandService.upload(uploadDto);
        return Result.ok();
    }

    @Aggregation(path = "/evaluation/insert", method = RequestMethod.POST,
        summary = "增加评价"
    )
    public Result insertEvaluation(@RequestBody SysCooperateDemandEvaluationDto sysCooperateDemandEvaluationDto) {
        sysCooperateDemandService.evaluate(sysCooperateDemandEvaluationDto);
        return Result.ok();
    }

    @Aggregation(path = "/evaluation/update", method = RequestMethod.POST,
            summary = "修改评价"
    )
    public Result updateEvaluation(@RequestBody SysCooperateDemandEvaluationDto sysCooperateDemandEvaluationDto) {
        sysCooperateDemandService.evaluate(sysCooperateDemandEvaluationDto);
        return Result.ok();
    }

    @Aggregation(path = "/query/unfinished", method = RequestMethod.POST,
        summary = "查询各种状态的订单", description = "可查用户/设计师"
    )
    public Result queryUnfinished(@RequestBody SysCooperateDemandQueryDto queryDto) {
        Page<SysCooperateDemand> page;
        if (queryDto.getUserId() != null) {
            page = sysCooperateDemandService.queryByUserId(queryDto.<SysCooperateDemand>getPage(),
                    queryDto.getUserId(), queryDto.getWorkCondition());
        }
        else {
            page = sysCooperateDemandService.queryByDesignerId(queryDto.<SysCooperateDemand>getPage(),
                    queryDto.getDesignerId(), queryDto.getWorkCondition());
        }
        return Result.data(page.getRecords());
    }

    @Aggregation(path = "/renewal", method = RequestMethod.POST,
    summary = "续期"
    )
    public Result renewal(@RequestBody RenewalDto renewalDto) {
        sysCooperateDemandService.renewal(renewalDto);
        return Result.ok();
    }
}
