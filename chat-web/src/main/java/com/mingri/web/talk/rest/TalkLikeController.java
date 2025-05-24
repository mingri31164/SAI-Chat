package com.mingri.web.talk.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.dto.LikeListDto;
import com.mingri.service.chat.repo.req.talk.CreateTalkLikeVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkLikeVo;
import com.mingri.service.chat.repo.req.talk.TalkLikeListVo;
import com.mingri.service.chat.service.TalkLikeService;
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
    public JSONObject createTalkLike(@Userid String userId, @RequestBody CreateTalkLikeVo createTalkLikeVo) {
        boolean result = talkLikeService.createTalkLike(userId, createTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    public JSONObject talkLikeList(@Userid String userId, @RequestBody TalkLikeListVo talkLikeListVo) {
        List<LikeListDto> result = talkLikeService.talkLikeList(userId, talkLikeListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalkLike(@Userid String userId, @RequestBody DeleteTalkLikeVo deleteTalkLikeVo) {
        boolean result = talkLikeService.deleteTalkLike(userId, deleteTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }
}

