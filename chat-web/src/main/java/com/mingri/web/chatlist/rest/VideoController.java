package com.mingri.web.chatlist.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.video.*;
import com.mingri.service.chat.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Tag(name = "音视频通话接口")
@RequestMapping("/v1/api/video")
public class VideoController {

    @Resource
    VideoService videoService;

    /**
     * 发送offer
     */
    @PostMapping("/offer")
    @Operation(summary = "发送WebRTC中的offer，发起文件传输")
    public JSONObject offer(@Userid String userId, @RequestBody OfferReq offerReq) {
        boolean result = videoService.offer(userId, offerReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 发送answer
     */
    @PostMapping("/answer")
    @Operation(summary = "发送WebRTC中的answer，响应offer")
    public JSONObject answer(@Userid String userId, @RequestBody AnswerReq answerReq) {
        boolean result = videoService.answer(userId, answerReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 发送candidate
     */
    @PostMapping("/candidate")
    @Operation(summary = "发送WebRTC中的candidate，用于建立网络连接的候选地址信息")
    public JSONObject candidate(@Userid String userId, @RequestBody CandidateReq candidateReq) {
        boolean result = videoService.candidate(userId, candidateReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 挂断
     */
    @PostMapping("/hangup")
    @Operation(summary = "挂断通话")
    public JSONObject hangup(@Userid String userId, @RequestBody HangupReq hangupReq) {
        boolean result = videoService.hangup(userId, hangupReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 邀请
     */
    @PostMapping("/invite")
    @Operation(summary = "邀请通话")
    public JSONObject invite(@Userid String userId, @RequestBody InviteReq inviteReq) {
        boolean result = videoService.invite(userId, inviteReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 接受
     */
    @PostMapping("/accept")
    @Operation(summary = "接受通话")
    public JSONObject accept(@Userid String userId, @RequestBody AcceptReq acceptReq) {
        boolean result = videoService.accept(userId, acceptReq);
        return ResultUtil.ResultByFlag(result);
    }
}
