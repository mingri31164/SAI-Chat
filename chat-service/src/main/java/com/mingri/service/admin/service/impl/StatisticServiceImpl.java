package com.mingri.service.admin.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.admin.repo.vo.dto.NumInfoDto;
import com.mingri.service.admin.repo.vo.entity.Statistic;
import com.mingri.service.chat.repo.vo.message.dto.Top10MsgDto;
import com.mingri.service.admin.repo.mapper.StatisticMapper;
import com.mingri.service.admin.service.StatisticService;
import com.mingri.service.chat.service.MessageService;
import com.mingri.service.user.service.UserOperatedService;
import com.mingri.service.websocket.WebSocketService;
import com.mingri.toolkit.SnowflakeIdUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class StatisticServiceImpl extends ServiceImpl<StatisticMapper, Statistic> implements StatisticService {

    @Resource
    UserOperatedService userOperatedService;

    @Resource
    StatisticMapper statisticMapper;

    @Resource
    WebSocketService webSocketService;

    @Resource
    MessageService messageService;

    @Override
    public void statisticLoginNum(Date yesterday) {
        Integer loginNum = userOperatedService.uniqueLoginNum(yesterday);
        LambdaQueryWrapper<Statistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Statistic::getDate, yesterday);
        Statistic statistic = getOne(queryWrapper);
        if (null == statistic) {
            statistic = new Statistic();
            statistic.setId(SnowflakeIdUtil.nextIdStr());
        }
        statistic.setDate(yesterday);
        statistic.setLoginNum(loginNum);
        saveOrUpdate(statistic);
    }


    public List<Statistic> getStatisticList(int day) {
        List<Statistic> result = statisticMapper.getStatisticList(day);
        return result;
    }

    @Override
    public NumInfoDto numInfo() {
        DateTime date = DateUtil.parseDate(DateUtil.today());
        List<Statistic> statisticList = getStatisticList(7);
        NumInfoDto numInfoDto = new NumInfoDto();
        numInfoDto.setStatistics(statisticList);
        numInfoDto.setLoginNum(userOperatedService.uniqueLoginNum(date));
        numInfoDto.setMsgNum(messageService.messageNum(date));
        numInfoDto.setOnlineNum(webSocketService.getOnlineNum());
        return numInfoDto;
    }

    @Override
    public List<Top10MsgDto> top10Msg() {
        List<Top10MsgDto> result = messageService.getTop10Msg(new Date());
        return result;
    }
}
