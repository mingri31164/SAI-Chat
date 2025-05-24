package com.mingri.model.vo.admin.dto;

import com.mingri.model.vo.admin.entity.Statistic;
import lombok.Data;

import java.util.List;

@Data
public class NumInfoDto {
    private Integer loginNum;
    private Integer onlineNum;
    private Integer msgNum;
    private List<Statistic> statistics;
}
