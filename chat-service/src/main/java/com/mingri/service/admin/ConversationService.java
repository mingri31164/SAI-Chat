package com.mingri.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.admin.req.conversation.DeleteConversationReq;
import com.mingri.model.vo.admin.req.conversation.DisableConversationReq;
import com.mingri.model.vo.admin.req.conversation.ResetSecretReq;
import com.mingri.model.vo.admin.req.conversation.UndisableConversationReq;
import com.mingri.model.vo.admin.req.entity.Conversation;
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
