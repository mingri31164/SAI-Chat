package com.mingri.service;

import cn.hutool.json.JSONObject;
import com.mingri.context.BaseContext;
import com.mingri.dto.file.*;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class FileService {

    @Resource
    WebSocketService webSocketService;

    public boolean offer(String userId, OfferDTO offerDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "offer");
        msg.set("desc", offerDTO.getDesc());
        msg.set("fromId", userId);
        webSocketService.sendFileToUser(msg, offerDTO.getUserId());
        return true;
    }

    public boolean answer(String userId, AnswerDTO answerDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "answer");
        msg.set("desc", answerDTO.getDesc());
        msg.set("fromId", userId);
        webSocketService.sendFileToUser(msg, answerDTO.getUserId());
        return true;
    }

    public boolean candidate(String userId, CandidateDTO CandidateDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "candidate");
        msg.set("candidate", CandidateDTO.getCandidate());
        msg.set("fromId", userId);
        webSocketService.sendFileToUser(msg, CandidateDTO.getUserId());
        return true;
    }

    public boolean cancel(String userId, CancelDTO cancelDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "cancel");
        msg.set("fromId", userId);
        webSocketService.sendFileToUser(msg, cancelDTO.getUserId());
        return true;
    }

    public boolean invite(InviteDTO inviteDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "invite");
        msg.set("fromId", BaseContext.getCurrentId());
        msg.set("fileInfo", inviteDTO.getFileInfo());
        webSocketService.sendFileToUser(msg, inviteDTO.getUserId());
        return true;
    }

    public boolean accept(String userId, AcceptDTO acceptDTO) {
        JSONObject msg = new JSONObject();
        msg.set("type", "accept");
        msg.set("fromId", userId);
        webSocketService.sendFileToUser(msg, acceptDTO.getUserId());
        return true;
    }
}
