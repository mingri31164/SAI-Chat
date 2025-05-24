package com.mingri.service.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.admin.dto.NumInfoDto;
import com.mingri.model.vo.admin.entity.Statistic;
import com.mingri.model.vo.chat.message.dto.Top10MsgDto;

import java.util.Date;
import java.util.List;

public interface StatisticService extends IService<Statistic> {
    void statisticLoginNum(Date yesterday);

    NumInfoDto numInfo();

    List<Top10MsgDto> top10Msg();
}
