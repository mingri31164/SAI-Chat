package com.mingri.service.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.admin.repo.vo.dto.ConversationDto;
import com.mingri.service.admin.repo.vo.req.conversation.DeleteConversationReq;
import com.mingri.service.admin.repo.vo.req.conversation.DisableConversationReq;
import com.mingri.service.admin.repo.vo.req.conversation.ResetSecretReq;
import com.mingri.service.admin.repo.vo.req.conversation.UndisableConversationReq;
import com.mingri.service.admin.repo.vo.entity.Conversation;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConversationService extends IService<Conversation> {
    Conversation getConversationByAccessKey(String accessKey);

    Conversation createConversation(MultipartFile portrait, String name);

    List<ConversationDto> conversationList();

    boolean updateConversation(MultipartFile portrait, String name, String id);

    boolean deleteConversation(DeleteConversationReq deleteConversationReq);

    boolean resetSecret(ResetSecretReq resetSecretReq);

    boolean disableConversation(DisableConversationReq disableConversationReq);

    boolean undisableConversation(UndisableConversationReq undisableConversationReq);
}
