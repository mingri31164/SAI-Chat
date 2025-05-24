package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.dto.LikeListDto;
import com.mingri.service.chat.repo.entity.Talk;
import com.mingri.service.chat.repo.entity.TalkLike;
import com.mingri.service.chat.repo.mapper.TalkLikeMapper;
import com.mingri.service.chat.repo.req.talk.CreateTalkLikeVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkLikeVo;
import com.mingri.service.chat.repo.req.talk.TalkLikeListVo;
import com.mingri.service.chat.service.TalkLikeService;
import com.mingri.service.chat.service.TalkService;
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
    public boolean createTalkLike(String userId, CreateTalkLikeVo createTalkLikeVo) {
        LambdaQueryWrapper<TalkLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TalkLike::getTalkId, createTalkLikeVo.getTalkId())
                .eq(TalkLike::getUserId, userId);
        if (count(queryWrapper) <= 0) {

            Talk talk = talkService.getById(createTalkLikeVo.getTalkId());
            talk.setLikeNum(talk.getLikeNum() + 1);
            talkService.updateById(talk);

            TalkLike talkLike = new TalkLike();
            talkLike.setId(IdUtil.randomUUID());
            talkLike.setTalkId(createTalkLikeVo.getTalkId());
            talkLike.setUserId(userId);
            return save(talkLike);
        }
        return false;
    }

    @Override
    public List<LikeListDto> talkLikeList(String userId, TalkLikeListVo talkLikeListVo) {
        List<LikeListDto> list = talkLikeList.talkLikeList(userId, talkLikeListVo.getTalkId());
        return list;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteTalkLike(String userId, DeleteTalkLikeVo deleteTalkLikeVo) {
        Talk talk = talkService.getById(deleteTalkLikeVo.getTalkId());
        talk.setLikeNum(talk.getLikeNum() - 1);
        talkService.updateById(talk);
        LambdaQueryWrapper<TalkLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TalkLike::getTalkId, deleteTalkLikeVo.getTalkId())
                .eq(TalkLike::getUserId, userId);
        return remove(queryWrapper);
    }
}
