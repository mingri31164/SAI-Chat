package com.mingri.web.chat.chatgroup.rest;

import com.mingri.pojo.entity.chat.ChatGroup;
import com.mingri.api.result.Result;
import com.mingri.pojo.vo.chatGroup.*;
import com.mingri.service.IChatGroupService;
import com.mingri.web.chat.chatgroup.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 聊天群表 前端控制器
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
@Tag(name = "群聊接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/chat-group")
public class ChatGroupController {

    @Resource
    private IChatGroupService chatGroupService;


    @Operation(summary = "获取当前用户的群聊列表")
    @GetMapping("/list")
    public Object chatGroupList(String userId) {
        List<ChatGroup> result = chatGroupService.chatGroupList(userId);
        return Result.success(result);
    }


    @Operation(summary = "创建聊天群")
    @PostMapping("/create")
    public Object createChatGroup(String userId, @RequestBody CreateChatGroupVo createChatGroupVo) {
        boolean result = chatGroupService.createChatGroup(userId, createChatGroupVo);
        return Result.success(result);
    }


    @Operation(summary = "更新群信息")
    @PostMapping("/update")
    public Object updateChatGroup(String userId, @RequestBody UpdateChatGroupVo updateChatGroupVo) {
        boolean result = chatGroupService.updateChatGroup(userId, updateChatGroupVo);
        return Result.success(result);
    }


    @Operation(summary = "解散群聊")
    @PostMapping("/dissolve")
    public Object dissolveChatGroup(String userId, @RequestBody DissolveChatGroupVo dissolveChatGroupVo) {
        boolean result = chatGroupService.dissolveChatGroup(userId, dissolveChatGroupVo);
        return Result.success(result);
    }


    @Operation(summary = "成员邀请")
    @PostMapping("/invite")
    public Object inviteMember(String userId, @RequestBody InviteMemberVo inviteMemberVo) {
        boolean result = chatGroupService.inviteMember(userId, inviteMemberVo);
        return Result.success(result);
    }


    @Operation(summary = "退出群聊")
    @PostMapping("/quit")
    public Object quitChatGroup(String userId, @RequestBody QuitChatGroupVo quitChatGroupVo) {
        boolean result = chatGroupService.quitChatGroup(userId, quitChatGroupVo);
        return Result.success(result);
    }


    @Operation(summary = "踢出群聊")
    @PostMapping("/kick")
    public Object kickChatGroup(String userId, @RequestBody KickChatGroupVo kickChatGroupVo) {
        boolean result = chatGroupService.kickChatGroup(userId, kickChatGroupVo);
        return Result.success(result);
    }


}
