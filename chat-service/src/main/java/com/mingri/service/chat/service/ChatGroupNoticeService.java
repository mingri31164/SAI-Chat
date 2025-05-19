package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.entity.ChatGroupNotice;
import com.mingri.service.chat.repo.req.chatGroupNotice.CreateNoticeVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.DeleteNoticeVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.NoticeListVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.UpdateNoticeVo;

import java.util.List;

/**
 * 聊天群公告表 服务类
 */
public interface ChatGroupNoticeService extends IService<ChatGroupNotice> {

    List<ChatGroupNotice> noticeList(String userId, NoticeListVo noticeListVo);

    boolean createNotice(String userId, CreateNoticeVo createNoticeVo);

    boolean deleteNotice(String userId, DeleteNoticeVo deleteNoticeVo);

    boolean updateNotice(String userId, UpdateNoticeVo updateNoticeVo);
}
