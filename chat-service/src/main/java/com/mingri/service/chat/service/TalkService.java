package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.TalkContentDto;
import com.mingri.service.chat.repo.dto.TalkListDto;
import com.mingri.service.chat.repo.entity.Talk;
import com.mingri.service.chat.repo.req.talk.CreateTalkVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkVo;
import com.mingri.service.chat.repo.req.talk.DetailsTalkVo;
import com.mingri.service.chat.repo.req.talk.TalkListVo;

import java.util.List;


/**
 * 说说 服务类
 */
public interface TalkService extends IService<Talk> {

    TalkContentDto getFriendLatestTalkContent(String userId, String friendId);

    List<TalkListDto> talkList(String userId, TalkListVo talkListVo);

    Talk createTalk(String userId, CreateTalkVo createTalkVo);

    Talk updateTalkImg(String userId, String talkId, String imgName);

    boolean deleteTalk(String userId, DeleteTalkVo deleteTalkVo);

    TalkListDto detailsTalk(String userId, DetailsTalkVo detailsTalkVo);
}
