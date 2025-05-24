package com.mingri.web.admin.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UrlResource;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.admin.dto.NumInfoDto;
import com.mingri.model.vo.chat.message.dto.Top10MsgDto;
import com.mingri.model.vo.user.dto.UserOperatedDto;
import com.mingri.model.vo.user.req.LoginDetailsReq;
import com.mingri.service.admin.service.StatisticService;
import com.mingri.service.user.service.UserOperatedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("AdminStatisticController")
@RequestMapping("/admin/v1/api/stat")
@Slf4j
public class StatisticController {

    @Resource
    UserOperatedService userOperatedService;

    @Resource
    StatisticService statisticService;

    /**
     * 登录详情列表
     *
     * @return
     */
    @PostMapping("/login/details")
    @UrlResource("admin")
    public JSONObject loginDetails(@RequestBody LoginDetailsReq loginDetailsReq) {
        List<UserOperatedDto> result = userOperatedService.loginDetails(loginDetailsReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 登录数量信息
     *
     * @return
     */
    @GetMapping("/num/info")
    @UrlResource("admin")
    public JSONObject numInfo() {
        NumInfoDto result = statisticService.numInfo();
        return ResultUtil.Succeed(result);
    }

    /**
     * 消息发送数量top10
     *
     * @return
     */
    @GetMapping("/top10/msg")
    @UrlResource("admin")
    public JSONObject top10Msg() {
        List<Top10MsgDto> result = statisticService.top10Msg();
        return ResultUtil.Succeed(result);
    }
}
