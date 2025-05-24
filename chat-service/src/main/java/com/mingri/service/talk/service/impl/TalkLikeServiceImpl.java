package com.mingri.service.talk.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.vo.talk.dto.LikeListDto;
import com.mingri.model.vo.talk.entity.Talk;
import com.mingri.model.vo.talk.entity.TalkLike;
import com.mingri.service.talk.repo.mapper.TalkLikeMapper;
import com.mingri.model.vo.talk.req.CreateTalkLikeReq;
import com.mingri.model.vo.talk.req.DeleteTalkLikeReq;
import com.mingri.model.vo.talk.req.TalkLikeListReq;
import com.mingri.service.talk.service.TalkLikeService;
import com.mingri.service.talk.service.TalkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 说说点赞 服务实现类
 */
@Service
public class TalkLikeServiceImpl extends ServiceImpl<TalkLikeMapper, TalkLike> implements TalkLikeService {

    @Resource
    TalkLikeMapper talkLikeList;

    @Resource
    TalkService talkService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createTalkLike(String userId, CreateTalkLikeReq createTalkLikeReq) {
        LambdaQueryWrapper<TalkLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TalkLike::getTalkId, createTalkLikeReq.getTalkId())
                .eq(TalkLike::getUserId, userId);
        if (count(queryWrapper) <= 0) {

            Talk talk = talkService.getById(createTalkLikeReq.getTalkId());
            talk.setLikeNum(talk.getLikeNum() + 1);
            talkService.updateById(talk);

            TalkLike talkLike = new TalkLike();
            talkLike.setId(IdUtil.randomUUID());
            talkLike.setTalkId(createTalkLikeReq.getTalkId());
            talkLike.setUserId(userId);
            return save(talkLike);
        }
        return false;
    }

    @Override
    public List<LikeListDto> talkLikeList(String userId, TalkLikeListReq talkLikeListReq) {
        List<LikeListDto> list = talkLikeList.talkLikeList(userId, talkLikeListReq.getTalkId());
        return list;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteTalkLike(String userId, DeleteTalkLikeReq deleteTalkLikeReq) {
        Talk talk = talkService.getById(deleteTalkLikeReq.getTalkId());
        talk.setLikeNum(talk.getLikeNum() - 1);
        talkService.updateById(talk);
        LambdaQueryWrapper<TalkLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TalkLike::getTalkId, deleteTalkLikeReq.getTalkId())
                .eq(TalkLike::getUserId, userId);
        return remove(queryWrapper);
    }
}
