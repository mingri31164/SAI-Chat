package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.CommentListDto;
import com.mingri.service.chat.repo.entity.TalkComment;
import com.mingri.service.chat.repo.req.talk.CreateTalkCommentVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkCommentVo;
import com.mingri.service.chat.repo.req.talk.TalkCommentListVo;

import java.util.List;


/**
 * 说说评论 服务类
 */
public interface TalkCommentService extends IService<TalkComment> {
    boolean createTalkComment(String userId, CreateTalkCommentVo createTalkCommentVo);

    List<CommentListDto> talkCommentList(String userId, TalkCommentListVo talkCommentListVo);

    boolean deleteTalkComment(String userId, DeleteTalkCommentVo deleteTalkLikeVo);
}
