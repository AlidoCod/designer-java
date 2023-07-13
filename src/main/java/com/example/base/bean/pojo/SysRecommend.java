package com.example.base.bean.pojo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SysRecommend {

    Long userId;
    String tag;
}
