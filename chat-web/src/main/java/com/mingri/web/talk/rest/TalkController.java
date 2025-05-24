package com.mingri.web.talk.rest;


import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.dto.TalkListDto;
import com.mingri.service.chat.repo.entity.Talk;
import com.mingri.service.chat.repo.req.talk.CreateTalkVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkVo;
import com.mingri.service.chat.repo.req.talk.DetailsTalkVo;
import com.mingri.service.chat.repo.req.talk.TalkListVo;
import com.mingri.service.chat.service.TalkService;
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
    public JSONObject talkList(@Userid String userId, @RequestBody TalkListVo talkListVo) {
        List<TalkListDto> result = talkService.talkList(userId, talkListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/details")
    public JSONObject detailsTalk(@Userid String userId, @RequestBody DetailsTalkVo detailsTalkVo) {
        TalkListDto result = talkService.detailsTalk(userId, detailsTalkVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/create")
    public JSONObject createTalk(@Userid String userId, @RequestBody CreateTalkVo createTalkVo) {
        Talk result = talkService.createTalk(userId, createTalkVo);
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
    public JSONObject deleteTalk(@Userid String userId, @RequestBody DeleteTalkVo deleteTalkVo) {
        boolean result = talkService.deleteTalk(userId, deleteTalkVo);
        return ResultUtil.ResultByFlag(result);
    }
}

