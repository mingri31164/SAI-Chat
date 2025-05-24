package com.mingri.service.talk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.talk.dto.LikeListDto;
import com.mingri.model.vo.talk.entity.TalkLike;
import com.mingri.model.vo.talk.req.CreateTalkLikeReq;
import com.mingri.model.vo.talk.req.DeleteTalkLikeReq;
import com.mingri.model.vo.talk.req.TalkLikeListReq;

import java.util.List;


/**
 * 说说点赞 服务类
 */
public interface TalkLikeService extends IService<TalkLike> {

    boolean createTalkLike(String userId, CreateTalkLikeReq createTalkLikeReq);

    List<LikeListDto> talkLikeList(String userId, TalkLikeListReq talkLikeListReq);

    boolean deleteTalkLike(String userId, DeleteTalkLikeReq deleteTalkLikeReq);
}
