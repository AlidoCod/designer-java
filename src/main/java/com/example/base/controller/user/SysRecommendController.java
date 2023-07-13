package com.example.base.controller.user;

import com.example.base.annotation.Aggregation;
import com.example.base.controller.bean.dto.user.SysUserFavourDto;
import com.example.base.controller.bean.vo.SysWorksVo;
import com.example.base.controller.bean.vo.base.Result;
import com.example.base.service.user.SysRecommendService;
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
@Tag(name = "推荐", description = "全部异步处理")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user/recommend")
@Controller
public class SysRecommendController {

    final SysRecommendService sysRecommendService;

    @Aggregation(path = "/init-favour", method = RequestMethod.POST,
            summary = "初始化偏好", description = "提供空格间隔的字符串"
    )
    public Result initFavour(@RequestBody @Valid SysUserFavourDto favour) {
        sysRecommendService.initFavour(favour.getId(), favour.getTag());
        return Result.ok();
    }

    @Aggregation(path = "/not-favour", method = RequestMethod.POST,
            summary = "不感兴趣", description = "降低标签评级")
    public Result notFavour(@RequestBody @Valid SysUserFavourDto favourDto) {
        sysRecommendService.updateFavour(favourDto.getId(), favourDto.getTag());
        return Result.ok();
    }

    @Aggregation(path = "/upload", method = RequestMethod.POST,
        summary = "上传浏览记录", description = "用于解析标签, 不作为历史记录"
    )
    public Result upload(@RequestBody @Valid SysUserFavourDto favourDto) {
        sysRecommendService.upload(favourDto.getId(), favourDto.getTag());
        return Result.ok();
    }

    @Aggregation(path = "/content", method = RequestMethod.GET,
        summary = "获取推荐内容", description = "返回所有按匹配分数排名的推荐作品ID"
    )
    public Result recommend(@RequestParam("id") @NotNull Long id) {
        List<SysWorksVo> list = sysRecommendService.recommend(id);
        return Result.data(list);
    }
}
