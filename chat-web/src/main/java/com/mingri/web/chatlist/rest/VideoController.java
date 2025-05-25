package com.mingri.web.chatlist.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.video.*;
import com.mingri.service.chat.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/api/video")
@Slf4j
public class VideoController {

    @Resource
    VideoService videoService;

    /**
     * 发送offer
     */
    @PostMapping("/offer")
    public JSONObject offer(@Userid String userId, @RequestBody OfferReq offerReq) {
        boolean result = videoService.offer(userId, offerReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 发送answer
     */
    @PostMapping("/answer")
    public JSONObject answer(@Userid String userId, @RequestBody AnswerReq answerReq) {
        boolean result = videoService.answer(userId, answerReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 发送candidate
     */
    @PostMapping("/candidate")
    public JSONObject candidate(@Userid String userId, @RequestBody CandidateReq candidateReq) {
        boolean result = videoService.candidate(userId, candidateReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 挂断
     */
    @PostMapping("/hangup")
    public JSONObject hangup(@Userid String userId, @RequestBody HangupReq hangupReq) {
        boolean result = videoService.hangup(userId, hangupReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 邀请
     */
    @PostMapping("/invite")
    public JSONObject invite(@Userid String userId, @RequestBody InviteReq inviteReq) {
        boolean result = videoService.invite(userId, inviteReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 邀请
     */
    @PostMapping("/accept")
    public JSONObject accept(@Userid String userId, @RequestBody AcceptReq acceptReq) {
        boolean result = videoService.accept(userId, acceptReq);
        return ResultUtil.ResultByFlag(result);
    }
}
