package com.mingri.web.talk.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.dto.CommentListDto;
import com.mingri.service.chat.repo.req.talk.CreateTalkCommentVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkCommentVo;
import com.mingri.service.chat.repo.req.talk.TalkCommentListVo;
import com.mingri.service.chat.service.TalkCommentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 说说评论 前端控制器
 */
@RestController
@RequestMapping("/v1/api/talk-comment")
public class TalkCommentController {

    @Resource
    TalkCommentService talkCommentService;

    @PostMapping("/create")
    public JSONObject createTalkComment(@Userid String userId, @RequestBody CreateTalkCommentVo createTalkCommentVo) {
        boolean result = talkCommentService.createTalkComment(userId, createTalkCommentVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    public JSONObject talkCommentList(@Userid String userId, @RequestBody TalkCommentListVo talkCommentListVo) {
        List<CommentListDto> result = talkCommentService.talkCommentList(userId, talkCommentListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalkComment(@Userid String userId, @RequestBody DeleteTalkCommentVo deleteTalkLikeVo) {
        boolean result = talkCommentService.deleteTalkComment(userId, deleteTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }
}

