package com.mingri.web.talk.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.talk.dto.CommentListDto;
import com.mingri.model.vo.talk.req.CreateTalkCommentReq;
import com.mingri.model.vo.talk.req.DeleteTalkCommentReq;
import com.mingri.model.vo.talk.req.TalkCommentListReq;
import com.mingri.service.talk.service.TalkCommentService;
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
    public JSONObject createTalkComment(@Userid String userId, @RequestBody CreateTalkCommentReq createTalkCommentVo) {
        boolean result = talkCommentService.createTalkComment(userId, createTalkCommentVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    public JSONObject talkCommentList(@Userid String userId, @RequestBody TalkCommentListReq talkCommentListReq) {
        List<CommentListDto> result = talkCommentService.talkCommentList(userId, talkCommentListReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalkComment(@Userid String userId, @RequestBody DeleteTalkCommentReq deleteTalkLikeVo) {
        boolean result = talkCommentService.deleteTalkComment(userId, deleteTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }
}

