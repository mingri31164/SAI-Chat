package com.mingri.service.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.admin.repo.vo.dto.NumInfoDto;
import com.mingri.service.admin.repo.vo.entity.Statistic;
import com.mingri.service.chat.repo.vo.message.dto.Top10MsgDto;

import java.util.Date;
import java.util.List;

public interface StatisticService extends IService<Statistic> {
    void statisticLoginNum(Date yesterday);

    NumInfoDto numInfo();

    List<Top10MsgDto> top10Msg();
}
