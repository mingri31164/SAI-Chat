package com.mingri.service.chat.service;

import cn.hutool.json.JSONObject;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.repo.vo.video.*;
import com.mingri.service.websocket.WebSocketService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class VideoService {

    @Resource
    FriendService friendService;

    @Resource
    WebSocketService webSocketService;

    public boolean offer(String userId, OfferReq offerReq) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, offerReq.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "offer");
        msg.set("desc", offerReq.getDesc());
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, offerReq.getUserId());
        return true;
    }

    public boolean answer(String userId, AnswerReq answerReq) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, answerReq.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "answer");
        msg.set("desc", answerReq.getDesc());
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, answerReq.getUserId());
        return true;
    }

    public boolean candidate(String userId, CandidateReq candidateReq) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, candidateReq.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "candidate");
        msg.set("candidate", candidateReq.getCandidate());
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, candidateReq.getUserId());
        return true;
    }

    public boolean hangup(String userId, HangupReq hangupReq) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, hangupReq.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "hangup");
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, hangupReq.getUserId());
        return true;
    }

    public boolean invite(String userId, InviteReq inviteReq) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, inviteReq.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "invite");
        msg.set("fromId", userId);
        msg.set("isOnlyAudio", inviteReq.isOnlyAudio());
        webSocketService.sendVideoToUser(msg, inviteReq.getUserId());
        return true;
    }

    public boolean accept(String userId, AcceptReq acceptReq) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, acceptReq.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "accept");
        msg.set("fromId", userId);
        webSocketService.sendVideoToUser(msg, acceptReq.getUserId());
        return true;
    }
}
