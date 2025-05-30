package com.mingri.service.admin.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.admin.repo.vo.entity.Statistic;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StatisticMapper extends BaseMapper<Statistic> {

    @Select("SELECT * " +
            "FROM `statistic` " +
            "WHERE `date` >= DATE_SUB(CURDATE(), INTERVAL #{day} DAY) " +
            "  AND `date` <= CURDATE() " +
            "ORDER BY `date` ASC ")
    List<Statistic> getStatisticList(int day);
}
