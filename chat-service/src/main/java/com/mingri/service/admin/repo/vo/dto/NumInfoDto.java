package com.mingri.service.admin.repo.vo.dto;

import com.mingri.service.admin.repo.vo.entity.Statistic;
import lombok.Data;

import java.util.List;

@Data
public class NumInfoDto {
    private Integer loginNum;
    private Integer onlineNum;
    private Integer msgNum;
    private List<Statistic> statistics;
}
