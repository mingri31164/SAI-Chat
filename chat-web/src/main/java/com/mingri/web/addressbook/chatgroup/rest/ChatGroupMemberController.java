package com.mingri.web.addressbook.chatgroup.rest;


import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.chat.chatgroup.dto.MemberListDto;
import com.mingri.model.vo.chat.chatgroup.entity.ChatGroupMember;
import com.mingri.model.vo.chat.chatlist.req.MemberListReq;
import com.mingri.service.chat.service.ChatGroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "聊天群成员接口")
@RequiredArgsConstructor
@RequestMapping("/v1/api/chat-group-member")
public class ChatGroupMemberController {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    private final MinioUtil minioUtil;

    @PostMapping("/list")
    @Operation(summary = "获取群成员列表（Map）")
    public JSONObject memberList(@Userid String userId, @RequestBody MemberListReq memberListVo) {
        Map<String, MemberListDto> result = chatGroupMemberService.memberList(userId, memberListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/list/page")
    @Operation(summary = "获取群成员列表（List）")
    public JSONObject memberListPage(@Userid String userId, @RequestBody MemberListReq memberListVo) {
        List<MemberListDto> result = chatGroupMemberService.memberListPage(userId, memberListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置聊天背景
     */
    @PostMapping("/set-chat-background")
    @Operation(summary = "设置聊天背景")
    public JSONObject setChatBackground(@Userid String userId,
                                        @RequestParam("groupId") String groupId,
                                        @RequestParam("name") String name,
                                        @RequestParam("type") String type,
                                        @RequestParam("size") long size,
                                        @RequestParam("file") MultipartFile file) {

        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getUserId, userId).eq(ChatGroupMember::getChatGroupId, groupId);
        ChatGroupMember chatGroupMember = chatGroupMemberService.getOne(queryWrapper);

        if (chatGroupMember == null) return ResultUtil.Fail("群聊不存在");
        boolean update;
        String url;
        try {
            String fileName = userId+"-"+groupId + "-chat-background" + name.substring(name.lastIndexOf("."));
            url = minioUtil.upload(file.getInputStream(), fileName, type, size);
            url += "?t=" + System.currentTimeMillis();
            chatGroupMember.setChatBackground(url);
            update = chatGroupMemberService.updateById(chatGroupMember);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!update) return ResultUtil.Fail("设置失败");
        return ResultUtil.Succeed(url);
    }
}
