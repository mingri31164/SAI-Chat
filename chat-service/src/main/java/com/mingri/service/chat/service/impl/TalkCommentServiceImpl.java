package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.dto.CommentListDto;
import com.mingri.service.chat.repo.entity.Talk;
import com.mingri.service.chat.repo.entity.TalkComment;
import com.mingri.service.chat.repo.mapper.TalkCommentMapper;
import com.mingri.service.chat.repo.req.talk.CreateTalkCommentVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkCommentVo;
import com.mingri.service.chat.repo.req.talk.TalkCommentListVo;
import com.mingri.service.chat.service.TalkCommentService;
import com.mingri.service.chat.service.TalkService;
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
    public boolean createTalkComment(String userId, CreateTalkCommentVo createTalkCommentVo) {


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
    public List<CommentListDto> talkCommentList(String userId, TalkCommentListVo talkCommentListVo) {
        List<CommentListDto> list = talkCommentMapper.talkCommentList(userId, talkCommentListVo.getTalkId());
        return list;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteTalkComment(String userId, DeleteTalkCommentVo deleteTalkLikeVo) {
        Talk talk = talkService.getById(deleteTalkLikeVo.getTalkId());
        talk.setCommentNum(talk.getCommentNum() - 1);
        talkService.updateById(talk);
        return removeById(deleteTalkLikeVo.getTalkCommentId());
    }
}
