package com.mingri.service.talk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.talk.repo.vo.dto.CommentListDto;
import com.mingri.service.talk.repo.vo.entity.TalkComment;
import com.mingri.service.talk.repo.vo.req.CreateTalkCommentReq;
import com.mingri.service.talk.repo.vo.req.DeleteTalkCommentReq;
import com.mingri.service.talk.repo.vo.req.TalkCommentListReq;

import java.util.List;


/**
 * 说说评论 服务类
 */
public interface TalkCommentService extends IService<TalkComment> {
    boolean createTalkComment(String userId, CreateTalkCommentReq createTalkCommentVo);

    List<CommentListDto> talkCommentList(String userId, TalkCommentListReq talkCommentListReq);

    boolean deleteTalkComment(String userId, DeleteTalkCommentReq deleteTalkLikeVo);
}
