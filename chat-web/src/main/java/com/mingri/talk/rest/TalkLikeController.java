package com.mingri.talk.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.talk.repo.vo.dto.LikeListDto;
import com.mingri.service.talk.repo.vo.req.CreateTalkLikeReq;
import com.mingri.service.talk.repo.vo.req.DeleteTalkLikeReq;
import com.mingri.service.talk.repo.vo.req.TalkLikeListReq;
import com.mingri.service.talk.service.TalkLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "说说点赞接口")
@RequestMapping("/v1/api/talk-like")
public class TalkLikeController {

    @Resource
    TalkLikeService talkLikeService;

    @PostMapping("/create")
    @Operation(summary = "创建说说点赞")
    public JSONObject createTalkLike(@Userid String userId, @RequestBody CreateTalkLikeReq createTalkLikeReq) {
        boolean result = talkLikeService.createTalkLike(userId, createTalkLikeReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    @Operation(summary = "获取说说点赞列表")
    public JSONObject talkLikeList(@Userid String userId, @RequestBody TalkLikeListReq talkLikeListReq) {
        List<LikeListDto> result = talkLikeService.talkLikeList(userId, talkLikeListReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "取消说说点赞")
    public JSONObject deleteTalkLike(@Userid String userId, @RequestBody DeleteTalkLikeReq deleteTalkLikeReq) {
        boolean result = talkLikeService.deleteTalkLike(userId, deleteTalkLikeReq);
        return ResultUtil.ResultByFlag(result);
    }

}

