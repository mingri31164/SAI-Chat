package com.mingri.service.talk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.talk.dto.CommentListDto;
import com.mingri.model.vo.talk.entity.TalkComment;
import com.mingri.model.vo.talk.req.CreateTalkCommentReq;
import com.mingri.model.vo.talk.req.DeleteTalkCommentReq;
import com.mingri.model.vo.talk.req.TalkCommentListReq;

import java.util.List;


/**
 * 说说评论 服务类
 */
public interface TalkCommentService extends IService<TalkComment> {
    boolean createTalkComment(String userId, CreateTalkCommentReq createTalkCommentVo);

    List<CommentListDto> talkCommentList(String userId, TalkCommentListReq talkCommentListReq);

    boolean deleteTalkComment(String userId, DeleteTalkCommentReq deleteTalkLikeVo);
}
