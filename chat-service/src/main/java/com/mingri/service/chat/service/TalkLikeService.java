package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.LikeListDto;
import com.mingri.service.chat.repo.entity.TalkLike;
import com.mingri.service.chat.repo.req.talk.CreateTalkLikeVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkLikeVo;
import com.mingri.service.chat.repo.req.talk.TalkLikeListVo;

import java.util.List;


/**
 * 说说点赞 服务类
 */
public interface TalkLikeService extends IService<TalkLike> {

    boolean createTalkLike(String userId, CreateTalkLikeVo createTalkLikeVo);

    List<LikeListDto> talkLikeList(String userId, TalkLikeListVo talkLikeListVo);

    boolean deleteTalkLike(String userId, DeleteTalkLikeVo deleteTalkLikeVo);
}
