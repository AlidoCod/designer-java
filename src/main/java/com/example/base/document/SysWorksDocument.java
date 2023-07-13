package com.example.base.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class SysWorksDocument {

    Long id;
    Long designerId;
    String title;
    String theme;
    String tag;
    String body;

    @JsonIgnore
    public void setUserId(Long userId){

    }

    @JsonIgnore
    public void getUserId(){}
}
