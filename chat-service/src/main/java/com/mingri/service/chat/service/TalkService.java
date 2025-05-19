package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.dto.TalkContentDto;
import com.mingri.service.chat.repo.entity.Talk;


/**
 * 说说 服务类
 */
public interface TalkService extends IService<Talk> {

    TalkContentDto getFriendLatestTalkContent(String userId, String friendId);
}
