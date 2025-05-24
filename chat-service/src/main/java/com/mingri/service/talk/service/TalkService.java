package com.mingri.service.talk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.talk.dto.TalkContentDto;
import com.mingri.model.vo.talk.dto.TalkListDto;
import com.mingri.model.vo.talk.entity.Talk;
import com.mingri.model.vo.talk.req.CreateTalkReq;
import com.mingri.model.vo.talk.req.DeleteTalkReq;
import com.mingri.model.vo.talk.req.DetailsTalkReq;
import com.mingri.model.vo.talk.req.TalkListReq;

import java.util.List;


/**
 * 说说 服务类
 */
public interface TalkService extends IService<Talk> {

    TalkContentDto getFriendLatestTalkContent(String userId, String friendId);

    List<TalkListDto> talkList(String userId, TalkListReq talkListReq);

    Talk createTalk(String userId, CreateTalkReq createTalkReq);

    Talk updateTalkImg(String userId, String talkId, String imgName);

    boolean deleteTalk(String userId, DeleteTalkReq deleteTalkReq);

    TalkListDto detailsTalk(String userId, DetailsTalkReq detailsTalkReq);
}
