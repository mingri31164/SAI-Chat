package com.mingri.web.chatgroup.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.exception.BaseException;
import com.mingri.model.vo.chat.chatgroup.dto.ChatGroupDetailsDto;
import com.mingri.model.vo.chat.chatgroup.req.*;
import com.mingri.model.vo.chat.chatgroup.entity.ChatGroup;
import com.mingri.service.chat.service.ChatGroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/api/chat-group")
public class ChatGroupController {

    @Resource
    ChatGroupService chatGroupService;

    @Resource
    MinioUtil minioUtil;

    /**
     * 聊天群列表
     */
    @GetMapping("/list")
    public JSONObject chatGroupList(@Userid String userId) {
        List<ChatGroup> result = chatGroupService.chatGroupList(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 创建聊天群
     */
    @PostMapping("/create")
    public JSONObject createChatGroup(@Userid String userId, @RequestBody CreateChatGroupReq createChatGroupVo) {
        boolean result = chatGroupService.createChatGroup(userId, createChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 更新群信息
     */
    @PostMapping("/update")
    public JSONObject updateChatGroup(@Userid String userId, @RequestBody UpdateChatGroupReq updateChatGroupVo) {
        boolean result = chatGroupService.updateChatGroup(userId, updateChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 更新群信息(群名称)
     */
    @PostMapping("/update/name")
    public JSONObject updateChatGroupName(@Userid String userId, @RequestBody UpdateChatGroupNameReq updateChatGroupNameVo) {
        boolean result = chatGroupService.updateChatGroupName(userId, updateChatGroupNameVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 成员邀请
     */
    @PostMapping("/invite")
    public JSONObject inviteMember(@Userid String userId, @RequestBody InviteMemberReq inviteMemberVo) {
        boolean result = chatGroupService.inviteMember(userId, inviteMemberVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 退出群聊
     */
    @PostMapping("/quit")
    public JSONObject quitChatGroup(@Userid String userId, @RequestBody QuitChatGroupReq quitChatGroupVo) {
        boolean result = chatGroupService.quitChatGroup(userId, quitChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 踢出群聊
     */
    @PostMapping("/kick")
    public JSONObject kickChatGroup(@Userid String userId, @RequestBody KickChatGroupReq kickChatGroupVo) {
        boolean result = chatGroupService.kickChatGroup(userId, kickChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 解散群聊
     */
    @PostMapping("/dissolve")
    public JSONObject dissolveChatGroup(@Userid String userId, @RequestBody DissolveChatGroupReq dissolveChatGroupVo) {
        boolean result = chatGroupService.dissolveChatGroup(userId, dissolveChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 转让群聊
     */
    @PostMapping("/transfer")
    public JSONObject transferChatGroup(@Userid String userId, @RequestBody TransferChatGroupReq transferChatGroupVo) {
        boolean result = chatGroupService.transferChatGroup(userId, transferChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 群详情
     */
    @PostMapping("/details")
    public JSONObject detailsChatGroup(@Userid String userId, @RequestBody DetailsChatGroupReq detailsChatGroupVo) {
        ChatGroupDetailsDto result = chatGroupService.detailsChatGroup(userId, detailsChatGroupVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 更新群头像
     */
    @PostMapping(value = "/upload/portrait")
    public JSONObject upload(HttpServletRequest request,
                             @Userid String userId,
                             @RequestHeader("groupId") String groupId,
                             @RequestHeader("name") String name,
                             @RequestHeader("type") String type,
                             @RequestHeader("size") long size) throws IOException {
        boolean isOwner = chatGroupService.isOwner(groupId, userId);
        if (!isOwner)
            throw new BaseException("您不是群主~");
        String fileName = groupId + "-portrait" + name.substring(name.lastIndexOf("."));
        String url = minioUtil.upload(request.getInputStream(), fileName, type, size);
        url += "?t=" + System.currentTimeMillis();
        chatGroupService.updateGroupPortrait(groupId, url);
        return ResultUtil.Succeed(url);
    }

    /**
     * 更新群头像（表单）
     */
    @PostMapping(value = "/upload/portrait/form")
    public JSONObject uploadForm(MultipartFile file,
                                 @Userid String userId,
                                 @RequestParam("groupId") String groupId,
                                 @RequestParam("name") String name,
                                 @RequestParam("type") String type,
                                 @RequestParam("size") long size) throws IOException {
        boolean isOwner = chatGroupService.isOwner(groupId, userId);
        if (!isOwner)
            throw new BaseException("您不是群主~");
        String fileName = groupId + "-portrait" + name.substring(name.lastIndexOf("."));
        String url = minioUtil.upload(file.getInputStream(), fileName, type, size);
        url += "?t=" + System.currentTimeMillis();
        chatGroupService.updateGroupPortrait(groupId, url);
        return ResultUtil.Succeed(url);
    }
}
