package com.mingri.web.talk.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.talk.dto.LikeListDto;
import com.mingri.model.vo.talk.req.CreateTalkLikeReq;
import com.mingri.model.vo.talk.req.DeleteTalkLikeReq;
import com.mingri.model.vo.talk.req.TalkLikeListReq;
import com.mingri.service.talk.service.TalkLikeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 说说点赞 前端控制器
 */
@RestController
@RequestMapping("/v1/api/talk-like")
public class TalkLikeController {

    @Resource
    TalkLikeService talkLikeService;

    @PostMapping("/create")
    public JSONObject createTalkLike(@Userid String userId, @RequestBody CreateTalkLikeReq createTalkLikeReq) {
        boolean result = talkLikeService.createTalkLike(userId, createTalkLikeReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    public JSONObject talkLikeList(@Userid String userId, @RequestBody TalkLikeListReq talkLikeListReq) {
        List<LikeListDto> result = talkLikeService.talkLikeList(userId, talkLikeListReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalkLike(@Userid String userId, @RequestBody DeleteTalkLikeReq deleteTalkLikeReq) {
        boolean result = talkLikeService.deleteTalkLike(userId, deleteTalkLikeReq);
        return ResultUtil.ResultByFlag(result);
    }
}

