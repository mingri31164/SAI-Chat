package com.mingri.web.chatlist.rest;


import cn.hutool.json.JSONObject;
import com.mingri.core.argument.UserRole;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.vo.chatlist.dto.ChatDto;
import com.mingri.service.chat.repo.vo.chatlist.dto.ChatListDto;
import com.mingri.service.chat.repo.vo.friend.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroup;
import com.mingri.service.chat.repo.vo.chatlist.entity.ChatList;
import com.mingri.service.chat.repo.mapper.ChatGroupMapper;
import com.mingri.service.chat.repo.vo.chatlist.req.CreateChatListReq;
import com.mingri.service.chat.repo.vo.chatlist.req.DeleteChatListReq;
import com.mingri.service.chat.repo.vo.chatlist.req.DetailChatListReq;
import com.mingri.service.chat.repo.vo.chatlist.req.TopChatListReq;
import com.mingri.service.chat.repo.vo.friend.req.SearchReq;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.chat.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@Tag(name = "聊天列表接口")
@RequiredArgsConstructor
@RequestMapping("/v1/api/chat-list")
public class ChatListController {
    @Resource
    ChatListService chatListService;

    private final FriendService friendService;

    private final ChatGroupMapper chatGroupMapper;

    /**
     * 获取聊天列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取聊天列表")
    public JSONObject getChatList(@Userid String userId) {
        ChatListDto chatList = chatListService.getChatList(userId);
        return ResultUtil.Succeed(chatList);
    }

    /**
     * 搜索好友或群组
     */
    @PostMapping("/search")
    @Operation(summary = "搜索好友或群组")
    public JSONObject searchFriends(@Userid String userId, @RequestBody SearchReq searchReq) {
        ChatDto chatDto = new ChatDto();
        List<FriendDetailsDto> friends = friendService.searchFriends(userId, searchReq);
        chatDto.setFriend(friends);
        List<ChatGroup> chatGroups = chatGroupMapper.getListFromSearch(userId, searchReq.getSearchInfo());
        chatDto.setGroup(chatGroups);
        return ResultUtil.Succeed(chatDto);
    }

    /**
     * 创建聊天会话
     */
    @PostMapping("/create")
    @Operation(summary = "创建聊天会话")
    public JSONObject createChatList(@Userid String userId, @UserRole String role, @RequestBody CreateChatListReq createChatListVo) {
        ChatList result = chatListService.createChatList(userId, role, createChatListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除会话
     */
    @PostMapping("/delete")
    @Operation(summary = "删除聊天会话")
    public JSONObject deleteChatList(@Userid String userId, @RequestBody DeleteChatListReq deleteChatListVo) {
        boolean result = chatListService.deleteChatList(userId, deleteChatListVo);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 设置置顶会话
     */
    @PostMapping("/top")
    @Operation(summary = "置顶聊天会话")
    public JSONObject topChatList(@Userid String userId, @RequestBody TopChatListReq topChatListVo) {
        boolean result = chatListService.topChatList(userId, topChatListVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 消息已读
     */
    @GetMapping("/read/{targetId}")
    @Operation(summary = "设置消息已读")
    public JSONObject messageRead(@Userid String userId, @PathVariable String targetId) {
        boolean result = chatListService.messageRead(userId, targetId);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 全部已读
     */
    @GetMapping("/read/all")
    @Operation(summary = "设置消息全部已读")
    public JSONObject messageReadAll(@Userid String userId) {
        boolean result = chatListService.messageReadAll(userId);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 获取详细信息
     */
    @PostMapping("/detail")
    @Operation(summary = "获取聊天会话详细信息")
    public JSONObject detailChatList(@Userid String userId, @RequestBody DetailChatListReq detailChatListVo) {
        ChatList result = chatListService.detailChartList(userId, detailChatListVo);
        return ResultUtil.Succeed(result);
    }
}

