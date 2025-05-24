package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.chat.chatgroup.entity.ChatGroupNotice;
import com.mingri.model.vo.chat.chatgroup.req.CreateNoticeReq;
import com.mingri.model.vo.chat.chatgroup.req.DeleteNoticeReq;
import com.mingri.model.vo.chat.chatgroup.req.NoticeListReq;
import com.mingri.model.vo.chat.chatgroup.req.UpdateNoticeReq;

import java.util.List;

/**
 * 聊天群公告表 服务类
 */
public interface ChatGroupNoticeService extends IService<ChatGroupNotice> {

    List<ChatGroupNotice> noticeList(String userId, NoticeListReq noticeListVo);

    boolean createNotice(String userId, CreateNoticeReq createNoticeVo);

    boolean deleteNotice(String userId, DeleteNoticeReq deleteNoticeVo);

    boolean updateNotice(String userId, UpdateNoticeReq updateNoticeVo);
}
