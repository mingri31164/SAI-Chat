package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.repo.entity.ChatGroup;
import com.mingri.service.chat.repo.entity.ChatGroupNotice;
import com.mingri.service.chat.repo.mapper.ChatGroupNoticeMapper;
import com.mingri.service.chat.repo.req.chatGroupNotice.CreateNoticeVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.DeleteNoticeVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.NoticeListVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.UpdateNoticeVo;
import com.mingri.service.chat.service.ChatGroupMemberService;
import com.mingri.service.chat.service.ChatGroupNoticeService;
import com.mingri.service.chat.service.ChatGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 聊天群公告表 服务实现类
 */
@Service
public class ChatGroupNoticeServiceImpl extends ServiceImpl<ChatGroupNoticeMapper, ChatGroupNotice> implements ChatGroupNoticeService {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Resource
    ChatGroupNoticeMapper chatGroupNoticeMapper;

    @Resource
    ChatGroupService chatGroupService;

    @Override
    public List<ChatGroupNotice> noticeList(String userId, NoticeListVo noticeListVo) {
        boolean isMemberExists = chatGroupMemberService.isMemberExists(noticeListVo.getGroupId(), userId);
        if (!isMemberExists)
            throw new BaseException("非该群成员~");
        List<ChatGroupNotice> result = chatGroupNoticeMapper.noticeList(noticeListVo.getGroupId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createNotice(String userId, CreateNoticeVo createNoticeVo) {
        ChatGroup chatGroup = chatGroupService.getById(createNoticeVo.getGroupId());
        if (!chatGroup.getOwnerUserId().equals(userId))
            throw new BaseException("您不是群主~");
        ChatGroupNotice chatGroupNotice = new ChatGroupNotice();
        chatGroupNotice.setId(IdUtil.randomUUID());
        chatGroupNotice.setChatGroupId(createNoticeVo.getGroupId());
        chatGroupNotice.setNoticeContent(createNoticeVo.getContent());
        chatGroupNotice.setUserId(userId);
        save(chatGroupNotice);
        chatGroup.setNotice(chatGroupNotice);
        return chatGroupService.updateById(chatGroup);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteNotice(String userId, DeleteNoticeVo deleteNoticeVo) {
        ChatGroup chatGroup = chatGroupService.getById(deleteNoticeVo.getGroupId());
        if (!chatGroup.getOwnerUserId().equals(userId))
            throw new BaseException("您不是群主~");
        LambdaQueryWrapper<ChatGroupNotice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupNotice::getId, deleteNoticeVo.getNoticeId())
                .eq(ChatGroupNotice::getUserId, userId)
                .eq(ChatGroupNotice::getChatGroupId, deleteNoticeVo.getGroupId());
        remove(queryWrapper);
        if (deleteNoticeVo.getNoticeId().equals(chatGroup.getNotice().getId())) {
            List<ChatGroupNotice> result = chatGroupNoticeMapper.noticeList(deleteNoticeVo.getGroupId());
            if (null != result && result.size() >= 0) {
                chatGroup.setNotice(result.get(0));
            }
            return chatGroupService.updateById(chatGroup);
        }
        return true;
    }

    @Override
    public boolean updateNotice(String userId, UpdateNoticeVo updateNoticeVo) {
        ChatGroup chatGroup = chatGroupService.getById(updateNoticeVo.getGroupId());
        if (!chatGroup.getOwnerUserId().equals(userId))
            throw new BaseException("您不是群主~");

        LambdaUpdateWrapper<ChatGroupNotice> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.set(ChatGroupNotice::getNoticeContent, updateNoticeVo.getNoticeContent())
                .eq(ChatGroupNotice::getUserId, userId)
                .eq(ChatGroupNotice::getChatGroupId, updateNoticeVo.getGroupId())
                .eq(ChatGroupNotice::getId, updateNoticeVo.getNoticeId());

        if (updateNoticeVo.getNoticeId().equals(chatGroup.getNotice().getId())) {
            chatGroup.getNotice().setNoticeContent(updateNoticeVo.getNoticeContent());
            chatGroupService.updateById(chatGroup);
        }
        return update(queryWrapper);
    }
}
