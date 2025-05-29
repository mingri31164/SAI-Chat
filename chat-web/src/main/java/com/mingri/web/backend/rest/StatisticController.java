package com.mingri.web.backend.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.permission.UrlResource;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.admin.dto.NumInfoDto;
import com.mingri.model.vo.chat.message.dto.Top10MsgDto;
import com.mingri.model.vo.user.dto.UserOperatedDto;
import com.mingri.model.vo.user.req.LoginDetailsReq;
import com.mingri.service.admin.service.StatisticService;
import com.mingri.service.user.service.UserOperatedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("AdminStatisticController")
@Tag(name = "管理端-首页数据接口")
@RequestMapping("/admin/v1/api/stat")
public class StatisticController {

    @Resource
    UserOperatedService userOperatedService;

    @Resource
    StatisticService statisticService;

    /**
     * 登录详情列表
     */
    @PostMapping("/login/details")
    @Operation(summary = "获取登录详情列表")
    @UrlResource("admin")
    public JSONObject loginDetails(@RequestBody LoginDetailsReq loginDetailsReq) {
        List<UserOperatedDto> result = userOperatedService.loginDetails(loginDetailsReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 登录数量信息
     */
    @GetMapping("/num/info")
    @Operation(summary = "获取登录数量信息")
    @UrlResource("admin")
    public JSONObject numInfo() {
        NumInfoDto result = statisticService.numInfo();
        return ResultUtil.Succeed(result);
    }

    /**
     * 消息发送数量top10
     */
    @GetMapping("/top10/msg")
    @Operation(summary = "获取消息发送数量top10")
    @UrlResource("admin")
    public JSONObject top10Msg() {
        List<Top10MsgDto> result = statisticService.top10Msg();
        return ResultUtil.Succeed(result);
    }
}
