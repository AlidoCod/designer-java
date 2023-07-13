package com.example.base.document;

import lombok.Data;

@Data
public class SysDemandDocument {

    Long id;
    Long userId;
    Integer demandCondition;

    String title;
    String theme;
    String body;
    String technicalSelection;
}
