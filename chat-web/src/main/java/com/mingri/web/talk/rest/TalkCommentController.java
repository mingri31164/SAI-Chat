package com.mingri.web.talk.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.talk.dto.CommentListDto;
import com.mingri.model.vo.talk.req.CreateTalkCommentReq;
import com.mingri.model.vo.talk.req.DeleteTalkCommentReq;
import com.mingri.model.vo.talk.req.TalkCommentListReq;
import com.mingri.service.talk.service.TalkCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "说说评论接口")
@RequestMapping("/v1/api/talk-comment")
public class TalkCommentController {

    @Resource
    TalkCommentService talkCommentService;

    @PostMapping("/create")
    @Operation(summary = "创建说说评论")
    public JSONObject createTalkComment(@Userid String userId, @RequestBody CreateTalkCommentReq createTalkCommentVo) {
        boolean result = talkCommentService.createTalkComment(userId, createTalkCommentVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    @Operation(summary = "获取说说评论列表")
    public JSONObject talkCommentList(@Userid String userId, @RequestBody TalkCommentListReq talkCommentListReq) {
        List<CommentListDto> result = talkCommentService.talkCommentList(userId, talkCommentListReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除说说评论")
    public JSONObject deleteTalkComment(@Userid String userId, @RequestBody DeleteTalkCommentReq deleteTalkLikeVo) {
        boolean result = talkCommentService.deleteTalkComment(userId, deleteTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }
}

