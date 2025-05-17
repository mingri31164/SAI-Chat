package com.mingri.web.chat.chatlist.rest;


import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UserRole;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.dto.ChatDto;
import com.mingri.service.chat.repo.dto.ChatListDto;
import com.mingri.service.chat.repo.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.entity.ChatGroup;
import com.mingri.service.chat.repo.entity.ChatList;
import com.mingri.service.chat.repo.mapper.ChatGroupMapper;
import com.mingri.service.chat.repo.req.*;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.chat.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/chat-list")
public class ChatListController {
    @Resource
    ChatListService chatListService;

    private final FriendService friendService;

    private final ChatGroupMapper chatGroupMapper;

    /**
     * 获取聊天列表
     *
     * @return
     */
    @GetMapping("/list")
    public JSONObject getChatList(@Userid String userId) {
        ChatListDto chatList = chatListService.getChatList(userId);
        return ResultUtil.Succeed(chatList);
    }

    /**
     * 搜索好友或群组
     *
     * @return
     */
    @PostMapping("/search")
    public JSONObject searchFriends(@Userid String userId, @RequestBody SearchVo searchVo) {
        ChatDto chatDto = new ChatDto();
        List<FriendDetailsDto> friends = friendService.searchFriends(userId, searchVo);
        chatDto.setFriend(friends);
        List<ChatGroup> chatGroups = chatGroupMapper.getListFromSearch(userId, searchVo.getSearchInfo());
        chatDto.setGroup(chatGroups);
        return ResultUtil.Succeed(chatDto);
    }

    /**
     * 创建聊天会话
     *
     * @return
     */
    @PostMapping("/create")
    public JSONObject createChatList(@Userid String userId, @UserRole String role, @RequestBody CreateChatListVo createChatListVo) {
        ChatList result = chatListService.createChatList(userId, role, createChatListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除会话
     *
     * @return
     */
    @PostMapping("/delete")
    public JSONObject deleteChatList(@Userid String userId, @RequestBody DeleteChatListVo deleteChatListVo) {
        boolean result = chatListService.deleteChatList(userId, deleteChatListVo);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 设置置顶会话
     *
     * @return
     */
    @PostMapping("/top")
    public JSONObject topChatList(@Userid String userId, @RequestBody TopChatListVo topChatListVo) {
        boolean result = chatListService.topChatList(userId, topChatListVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 消息已读
     *
     * @return
     */
    @GetMapping("/read/{targetId}")
    public JSONObject messageRead(@Userid String userId, @PathVariable String targetId) {
        boolean result = chatListService.messageRead(userId, targetId);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 全部已读
     *
     * @return
     */
    @GetMapping("/read/all")
    public JSONObject messageReadAll(@Userid String userId) {
        boolean result = chatListService.messageReadAll(userId);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 获取详细信息
     *
     * @return
     */
    @PostMapping("/detail")
    public JSONObject detailChatList(@Userid String userId, @RequestBody DetailChatListVo detailChatListVo) {
        ChatList result = chatListService.detailChartList(userId, detailChatListVo);
        return ResultUtil.Succeed(result);
    }
}

