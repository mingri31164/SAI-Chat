package com.mingri.web.backend.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.permission.UrlResource;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.admin.repo.vo.dto.ConversationDto;
import com.mingri.service.admin.repo.vo.entity.Conversation;
import com.mingri.service.admin.repo.vo.req.conversation.DeleteConversationReq;
import com.mingri.service.admin.repo.vo.req.conversation.DisableConversationReq;
import com.mingri.service.admin.repo.vo.req.conversation.ResetSecretReq;
import com.mingri.service.admin.repo.vo.req.conversation.UndisableConversationReq;
import com.mingri.service.admin.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;



@RestController("AdminConversationController")
@Tag(name = "管理端-会话管理接口")
@RequestMapping("/admin/v1/api/conversation")
public class ConversationController {

    @Resource
    ConversationService conversationService;

    /**
     * 创建会话
     */
    @PostMapping("/create")
    @Operation(summary = "创建会话")
    @UrlResource("admin")
    public JSONObject createConversation(@NotNull(message = "头像不能为空~") @RequestParam("portrait") MultipartFile portrait,
                                         @NotNull(message = "名称不能为空~") @RequestParam("name") String name) {
        Conversation result = conversationService.createConversation(portrait, name);
        return ResultUtil.Succeed(result);
    }

    /**
     * 修改会话
     */
    @PostMapping("/update")
    @Operation(summary = "修改会话")
    @UrlResource("admin")
    public JSONObject updateConversation(@NotNull(message = "头像不能为空~") @RequestParam("portrait") MultipartFile portrait,
                                         @NotNull(message = "名称不能为空~") @RequestParam("name") String name,
                                         @NotNull(message = "会话不能为空~") @RequestParam("id") String id) {
        boolean result = conversationService.updateConversation(portrait, name, id);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 会话列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取会话列表")
    @UrlResource("admin")
    public JSONObject conversationList() {
        List<ConversationDto> result = conversationService.conversationList();
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除会话
     */
    @PostMapping("/delete")
    @Operation(summary = "删除会话")
    @UrlResource("admin")
    public JSONObject deleteConversation(@RequestBody DeleteConversationReq deleteConversationReq) {
        boolean result = conversationService.deleteConversation(deleteConversationReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 重置会话秘钥
     */
    @PostMapping("/reset/secret")
    @Operation(summary = "重置会话秘钥")
    @UrlResource("admin")
    public JSONObject resetSecret(@RequestBody ResetSecretReq resetSecretReq) {
        boolean result = conversationService.resetSecret(resetSecretReq);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 禁用会话
     */
    @PostMapping("/disable")
    @Operation(summary = "禁用会话")
    @UrlResource("admin")
    public JSONObject disableConversation(@RequestBody DisableConversationReq disableConversationReq) {
        boolean result = conversationService.disableConversation(disableConversationReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 解禁会话
     */
    @PostMapping("/undisable")
    @Operation(summary = "解禁会话")
    @UrlResource("admin")
    public JSONObject undisableConversation(@RequestBody UndisableConversationReq undisableConversationReq) {
        boolean result = conversationService.undisableConversation(undisableConversationReq);
        return ResultUtil.ResultByFlag(result);
    }
}
