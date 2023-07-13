package com.example.base.controller.bean.dto.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;



@Schema(description = "分页")
@Data
public class BasePage {

    @Schema(description = "当前页")
    Integer current = 1;
    @Schema(description = "每页的数量")
    Integer size = 10;

    public <T> Page<T> getPage() {
        return new Page<>(current, size);
    }
}
