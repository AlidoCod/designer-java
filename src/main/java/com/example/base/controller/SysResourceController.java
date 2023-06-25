package com.example.base.controller;

import com.example.base.annotation.Aggregation;
import com.example.base.bean.vo.result.Result;
import com.example.base.service.SysResourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "文件资源")
@Validated
@RequiredArgsConstructor
@RequestMapping("/resource")
@Controller
public class SysResourceController {

    final SysResourceService resourceService;

    @Aggregation(path = "/check", method = RequestMethod.GET, summary = "校验资源是否存在", description = "true: 校验成功, 无返回值，false: 检验失败, 返回已存在的文件ID")
    public Result check(@RequestParam("md5")String md5) {
        Long id = resourceService.check(md5);
        return id == null ? Result.success(String.valueOf(true)) : Result.data(id, String.valueOf(false));
    }

    @Aggregation(path = "/upload", method = RequestMethod.POST)
    public Result upload(@RequestParam(value = "md5", required = false) String md5, @RequestParam("suffix")String suffix, @RequestPart(value = "file")MultipartFile file) {
        Long id = resourceService.upload(md5, suffix, file);
        return Result.success(String.valueOf(id));
    }

    @Aggregation(path = "/download", method = RequestMethod.GET)
    public void download(@RequestParam("id") Long id, HttpServletResponse response) {
        resourceService.download(response, id);
    }
}
