package com.example.base.controller.bean.dto.search;

import com.example.base.controller.bean.dto.base.BasePage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContentSearchDto extends BasePage {

    @NotBlank
    String content;
}
