package com.example.base.controller.bean.vo.base;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tuple {

    String key;
    String value;

    public static Tuple of(String key, String value) {
        return Tuple.builder()
                .key(key)
                .value(value)
                .build();
    }
}
