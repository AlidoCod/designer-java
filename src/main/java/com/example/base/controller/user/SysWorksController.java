package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.dto.search.ContentSearchDto;
import com.example.base.controller.bean.dto.works.SysWorksQueryDto;
import com.example.base.controller.bean.dto.works.SysWorksUpdateDto;
import com.example.base.controller.bean.dto.works.SysWorksUploadDto;
import com.example.base.controller.bean.vo.SysWorksVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysWorksService;
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
@Tag(name = "作品")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/works")
@Controller
public class SysWorksController {

    final SysWorksService sysWorksService;

    @Aggregation(path = "/upload", method = RequestMethod.POST,
        summary = "上传作品", description = "两种上传方式，这是无需求的作品"
    )
    public Result upload(@RequestBody @Valid SysWorksUploadDto uploadDto) {
        Long id = sysWorksService.upload(uploadDto);
        return Result.data(id);
    }

    @Aggregation(path = "/query", method = RequestMethod.POST,
        summary = "查询作品", description = "优先级：1.传入作品ID则查询作品 2.传入设计师ID和分页参数则查询设计师的作品"
    )
    public Result query(@RequestBody @Valid SysWorksQueryDto queryDto) {
        if (queryDto.getId() != null) {
            SysWorksVo sysWorks = sysWorksService.queryById(queryDto.getId());
            return Result.data(sysWorks);
        }
        else {
            List<SysWorksVo> list= sysWorksService.queryByDesignerId(queryDto.getPage(), queryDto.getDesignerId());
            return Result.data(list);
        }
    }

    @Aggregation(path = "/update", method = RequestMethod.POST,
        summary = "修改作品"
    )
    public Result update(@RequestBody @Valid SysWorksUpdateDto updateDto) {
        sysWorksService.update(updateDto);
        return Result.ok();
    }

    @Aggregation(path = "/delete", method = RequestMethod.GET,
        summary = "删除作品", description = "仅未绑定需求的作品可删除"
    )
    public Result delete(@RequestParam("id") @NotNull Long id) {
        sysWorksService.delete(id);
        return Result.ok();
    }

    @Aggregation(path = "/search", method = RequestMethod.POST,
        summary = "搜索作品"
    )
    public Result search(@RequestBody @Valid ContentSearchDto searchDto) {
        List<SysWorksVo> list = sysWorksService.search(searchDto.getPage(), searchDto.getContent());
        return Result.data(list);
    }
}
