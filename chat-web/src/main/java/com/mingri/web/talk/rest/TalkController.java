package com.mingri.web.talk.rest;


import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.talk.dto.TalkListDto;
import com.mingri.model.vo.talk.entity.Talk;
import com.mingri.model.vo.talk.req.CreateTalkReq;
import com.mingri.model.vo.talk.req.DeleteTalkReq;
import com.mingri.model.vo.talk.req.DetailsTalkReq;
import com.mingri.model.vo.talk.req.TalkListReq;
import com.mingri.service.talk.service.TalkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 说说 前端控制器
 */
@RestController
@RequestMapping("/v1/api/talk")
public class TalkController {

    @Resource
    TalkService talkService;

    @Resource
    MinioUtil minioUtil;

    @PostMapping("/list")
    public JSONObject talkList(@Userid String userId, @RequestBody TalkListReq talkListReq) {
        List<TalkListDto> result = talkService.talkList(userId, talkListReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/details")
    public JSONObject detailsTalk(@Userid String userId, @RequestBody DetailsTalkReq detailsTalkReq) {
        TalkListDto result = talkService.detailsTalk(userId, detailsTalkReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/create")
    public JSONObject createTalk(@Userid String userId, @RequestBody CreateTalkReq createTalkReq) {
        Talk result = talkService.createTalk(userId, createTalkReq);
        return ResultUtil.Succeed(result);
    }


    @PostMapping("/upload/img")
    public JSONObject uploadImgTalk(HttpServletRequest request,
                                    @Userid String userId,
                                    @RequestHeader("talkId") String talkId,
                                    @RequestHeader("name") String name,
                                    @RequestHeader("type") String type,
                                    @RequestHeader("size") long size) throws IOException {
        String imgName = IdUtil.randomUUID() + name.substring(name.lastIndexOf("."));
        String imgPath = userId + "/img/" + imgName;
        minioUtil.uploadFile(request.getInputStream(), imgPath, size);
        Talk talk = talkService.updateTalkImg(userId, talkId, imgName);
        return ResultUtil.Succeed(talk);
    }

    @PostMapping("/upload/img/form")
    public JSONObject uploadImgTalk(MultipartFile file,
                                    @Userid String userId,
                                    @RequestParam("talkId") String talkId,
                                    @RequestParam("name") String name,
                                    @RequestParam("size") long size) throws IOException {
        String imgName = IdUtil.randomUUID() + name.substring(name.lastIndexOf("."));
        String imgPath = userId + "/img/" + imgName;
        minioUtil.uploadFile(file.getInputStream(), imgPath, size);
        Talk talk = talkService.updateTalkImg(userId, talkId, imgName);
        return ResultUtil.Succeed(talk);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalk(@Userid String userId, @RequestBody DeleteTalkReq deleteTalkReq) {
        boolean result = talkService.deleteTalk(userId, deleteTalkReq);
        return ResultUtil.ResultByFlag(result);
    }
}

