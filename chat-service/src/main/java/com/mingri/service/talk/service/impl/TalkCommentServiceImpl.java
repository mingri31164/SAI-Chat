package com.mingri.service.talk.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.vo.talk.dto.CommentListDto;
import com.mingri.model.vo.talk.entity.Talk;
import com.mingri.model.vo.talk.entity.TalkComment;
import com.mingri.service.talk.repo.mapper.TalkCommentMapper;
import com.mingri.model.vo.talk.req.CreateTalkCommentReq;
import com.mingri.model.vo.talk.req.DeleteTalkCommentReq;
import com.mingri.model.vo.talk.req.TalkCommentListReq;
import com.mingri.service.talk.service.TalkCommentService;
import com.mingri.service.talk.service.TalkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 说说评论 服务实现类
 */
@Service
public class TalkCommentServiceImpl extends ServiceImpl<TalkCommentMapper, TalkComment> implements TalkCommentService {

    @Resource
    TalkCommentMapper talkCommentMapper;

    @Resource
    TalkService talkService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createTalkComment(String userId, CreateTalkCommentReq createTalkCommentVo) {


        Talk talk = talkService.getById(createTalkCommentVo.getTalkId());
        talk.setCommentNum(talk.getCommentNum() + 1);
        talkService.updateById(talk);

        TalkComment talkComment = new TalkComment();
        talkComment.setId(IdUtil.randomUUID());
        talkComment.setTalkId(createTalkCommentVo.getTalkId());
        talkComment.setUserId(userId);
        talkComment.setContent(createTalkCommentVo.getComment());


        return save(talkComment);
    }

    @Override
    public List<CommentListDto> talkCommentList(String userId, TalkCommentListReq talkCommentListReq) {
        List<CommentListDto> list = talkCommentMapper.talkCommentList(userId, talkCommentListReq.getTalkId());
        return list;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteTalkComment(String userId, DeleteTalkCommentReq deleteTalkLikeVo) {
        Talk talk = talkService.getById(deleteTalkLikeVo.getTalkId());
        talk.setCommentNum(talk.getCommentNum() - 1);
        talkService.updateById(talk);
        return removeById(deleteTalkLikeVo.getTalkCommentId());
    }
}
