package com.mingri.controller.user;

import com.mingri.annotation.UrlLimit;
import com.mingri.dto.call.*;
import com.mingri.result.Result;
import com.mingri.service.CallService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/29 21:24
 * @ClassName: CallController
 * @Version: 1.0
 */


@Api(tags = "通话接口")
@RestController
@Slf4j
@RequestMapping("/api/v1/call")
public class CallController {

    @Resource
    CallService callService;

    /**
     * 发送offer
     */
    @UrlLimit
    @ApiOperation("发送WebRTC中的offer，用于发起通话请求")
    @PostMapping("/offer")
    public Object offer(String userId, @RequestBody OfferDTO offerDTO) {
        boolean result = callService.offer(userId, offerDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 发送answer
     */
    @UrlLimit
    @ApiOperation("发送WebRTC中的answer，响应offer")
    @PostMapping("/answer")
    public Object answer(String userId, @RequestBody AnswerDTO answerDTO) {
        boolean result = callService.answer(userId, answerDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 发送candidate
     */
    @UrlLimit
    @ApiOperation("发送WebRTC中的candidate，用于建立网络连接的候选地址信息")
    @PostMapping("/candidate")
    public Object candidate(String userId, @RequestBody CandidateDTO candidateDTO) {
        boolean result = callService.candidate(userId, candidateDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 挂断
     */
    @UrlLimit
    @ApiOperation("挂断通话")
    @PostMapping("/hangup")
    public Object hangup(String userId, @RequestBody HangupDTO hangupDTO) {
        boolean result = callService.hangup(userId, hangupDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 邀请
     */
    @UrlLimit
    @ApiOperation("邀请通话")
    @PostMapping("/invite")
    public Object invite(@RequestBody InviteDTO inviteDTO) {
        boolean result = callService.invite(inviteDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 接收
     */
    @UrlLimit
    @ApiOperation("接受通话")
    @PostMapping("/accept")
    public Object accept(String userId, @RequestBody AcceptDTO acceptDTO) {
        boolean result = callService.accept(userId, acceptDTO);
        return result? Result.success():Result.error();
    }

}
