package com.mingri.web.backend.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UrlResource;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.admin.dto.ConversationDto;
import com.mingri.model.vo.admin.entity.Conversation;
import com.mingri.model.vo.admin.req.conversation.DeleteConversationReq;
import com.mingri.model.vo.admin.req.conversation.DisableConversationReq;
import com.mingri.model.vo.admin.req.conversation.ResetSecretReq;
import com.mingri.model.vo.admin.req.conversation.UndisableConversationReq;
import com.mingri.service.admin.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("AdminConversationController")
@RequestMapping("/admin/v1/api/conversation")
@Slf4j
public class ConversationController {

    @Resource
    ConversationService conversationService;

    /**
     * 创建会话
     *
     * @return
     */
    @PostMapping("/create")
    @UrlResource("admin")
    public JSONObject createConversation(@NotNull(message = "头像不能为空~") @RequestParam("portrait") MultipartFile portrait,
                                         @NotNull(message = "名称不能为空~") @RequestParam("name") String name) {
        Conversation result = conversationService.createConversation(portrait, name);
        return ResultUtil.Succeed(result);
    }

    /**
     * 修改会话
     *
     * @return
     */
    @PostMapping("/update")
    @UrlResource("admin")
    public JSONObject updateConversation(@NotNull(message = "头像不能为空~") @RequestParam("portrait") MultipartFile portrait,
                                         @NotNull(message = "名称不能为空~") @RequestParam("name") String name,
                                         @NotNull(message = "会话不能为空~") @RequestParam("id") String id) {
        boolean result = conversationService.updateConversation(portrait, name, id);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 会话列表
     *
     * @return
     */
    @GetMapping("/list")
    @UrlResource("admin")
    public JSONObject conversationList() {
        List<ConversationDto> result = conversationService.conversationList();
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除会话
     *
     * @return
     */
    @PostMapping("/delete")
    @UrlResource("admin")
    public JSONObject deleteConversation(@RequestBody DeleteConversationReq deleteConversationReq) {
        boolean result = conversationService.deleteConversation(deleteConversationReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 重置会话秘钥
     *
     * @return
     */
    @PostMapping("/reset/secret")
    @UrlResource("admin")
    public JSONObject resetSecret(@RequestBody ResetSecretReq resetSecretReq) {
        boolean result = conversationService.resetSecret(resetSecretReq);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 禁用会话
     *
     * @return
     */
    @PostMapping("/disable")
    @UrlResource("admin")
    public JSONObject disableConversation(@RequestBody DisableConversationReq disableConversationReq) {
        boolean result = conversationService.disableConversation(disableConversationReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 解禁会话
     *
     * @return
     */
    @PostMapping("/undisable")
    @UrlResource("admin")
    public JSONObject undisableConversation(@RequestBody UndisableConversationReq undisableConversationReq) {
        boolean result = conversationService.undisableConversation(undisableConversationReq);
        return ResultUtil.ResultByFlag(result);
    }
}
