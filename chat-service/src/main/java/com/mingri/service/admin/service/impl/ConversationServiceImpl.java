package com.mingri.service.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.constant.ConversationStatus;
import com.mingri.model.exception.BaseException;
import com.mingri.service.admin.repo.vo.dto.ConversationDto;
import com.mingri.service.admin.repo.vo.req.conversation.DeleteConversationReq;
import com.mingri.service.admin.repo.vo.req.conversation.DisableConversationReq;
import com.mingri.service.admin.repo.vo.req.conversation.ResetSecretReq;
import com.mingri.service.admin.repo.vo.req.conversation.UndisableConversationReq;
import com.mingri.service.admin.repo.vo.entity.Conversation;
import com.mingri.service.admin.repo.mapper.ConversationMapper;
import com.mingri.service.admin.service.ConversationService;
import com.mingri.service.user.service.UserService;
import com.mingri.toolkit.SnowflakeIdUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Resource
    UserService userService;

    @Resource
    ConversationMapper conversationMapper;

    @Override
    public Conversation getConversationByAccessKey(String accessKey) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getAccessKey, accessKey);
        Conversation conversation = getOne(queryWrapper);
        if (null == conversation || !ConversationStatus.Normal.equals(conversation.getStatus())) {
            return null;
        }
        return conversation;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Conversation createConversation(MultipartFile portrait, String name) {
        String userId = userService.createThirdPartyUser(portrait, name);
        Conversation conversation = new Conversation();
        conversation.setId(SnowflakeIdUtil.nextIdStr());
        conversation.setUserId(userId);
        conversation.setAccessKey(SnowflakeIdUtil.nextIdStr().replace("-", ""));
        conversation.setSecretKey(SnowflakeIdUtil.nextIdStr().replace("-", ""));
        conversation.setStatus(ConversationStatus.Normal);
        save(conversation);
        return conversation;
    }

    @Override
    public List<ConversationDto> conversationList() {
        List<ConversationDto> result = conversationMapper.conversationList();
        return result;
    }

    @Override
    public boolean updateConversation(MultipartFile portrait, String name, String id) {
        Conversation conversation = getById(id);
        if (null == conversation) {
            throw new BaseException("会话不存在~");
        }
        boolean result = userService.updateThirdPartyUser(portrait, name, conversation.getUserId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteConversation(DeleteConversationReq deleteConversationReq) {
        Conversation conversation = getById(deleteConversationReq.getConversationId());
        if (null == conversation) {
            throw new BaseException("会话不存在~");
        }
        boolean result = userService.deleteThirdPartyUser(conversation.getUserId());
        if (result) {
            return removeById(conversation.getId());
        }
        return false;
    }

    @Override
    public boolean resetSecret(ResetSecretReq resetSecretReq) {
        Conversation conversation = getById(resetSecretReq.getConversationId());
        if (null == conversation) {
            throw new BaseException("会话不存在~");
        }
        conversation.setAccessKey(SnowflakeIdUtil.nextIdStr().replace("-", ""));
        conversation.setSecretKey(SnowflakeIdUtil.nextIdStr().replace("-", ""));
        return updateById(conversation);
    }

    @Override
    public boolean disableConversation(DisableConversationReq disableConversationReq) {
        Conversation conversation = getById(disableConversationReq.getConversationId());
        if (null == conversation) {
            throw new BaseException("会话不存在~");
        }
        conversation.setStatus(ConversationStatus.Disable);
        return updateById(conversation);
    }

    @Override
    public boolean undisableConversation(UndisableConversationReq undisableConversationReq) {
        Conversation conversation = getById(undisableConversationReq.getConversationId());
        if (null == conversation) {
            throw new BaseException("会话不存在~");
        }
        conversation.setStatus(ConversationStatus.Normal);
        return updateById(conversation);
    }
}
