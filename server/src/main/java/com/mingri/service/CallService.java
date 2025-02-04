package com.mingri.service;

import cn.hutool.json.JSONObject;
import com.mingri.context.BaseContext;
import com.mingri.dto.call.*;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/29 21:26
 * @ClassName: CallService
 * @Version: 1.0
 */

@Service
public class CallService {

    @Resource
    WebSocketService webSocketService;

    public boolean offer(String userId, OfferDTO offerDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "offer");
        msg.set("desc", offerDTO.getDesc());
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, offerDTO.getUserId());
        return true;
    }

    public boolean answer(String userId, AnswerDTO answerDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "answer");
        msg.set("desc", answerDTO.getDesc());
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, answerDTO.getUserId());
        return true;
    }

    public boolean candidate(String userId, CandidateDTO candidateDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "candidate");
        msg.set("candidate", candidateDTO.getCandidate());
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, candidateDTO.getUserId());
        return true;
    }

    public boolean hangup(String userId, HangupDTO hangupDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "hangup");
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, hangupDTO.getUserId());
        return true;
    }

    public boolean invite(InviteDTO inviteDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "invite");
        msg.set("fromId", BaseContext.getCurrentId());
        msg.set("isOnlyAudio", inviteDTO.isOnlyAudio());
        webSocketService.sendVideoToUser(msg, inviteDTO.getUserId());
        return true;
    }

    public boolean accept(String userId, AcceptDTO acceptDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "accept");
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, acceptDTO.getUserId());
        return true;
    }

}
