package com.mingri.dto.message;

import lombok.Data;

import javax.validation.constraints.Max;

@Data
public class RecordDTO {
    //目标id
    private String targetId;
    //起始
    private int index;
    //查询条数
    @Max(100)
    private int num;
}
