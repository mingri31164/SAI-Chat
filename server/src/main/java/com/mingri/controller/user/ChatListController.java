package com.mingri.controller.user;

import com.mingri.annotation.UrlLimit;
import com.mingri.dto.chatList.CreateDTO;
import com.mingri.dto.chatList.DeleteDTO;
import com.mingri.dto.chatList.ReadDTO;
import com.mingri.entity.ChatList;
import com.mingri.result.Result;
import com.mingri.service.IChatListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;



@Api(tags = "消息列表接口")
@RestController
@RequestMapping("/api/v1/chat-list")
public class ChatListController {

    @Resource
    IChatListService chatListService;


    @UrlLimit
    @ApiOperation("获取私聊列表")
    @GetMapping("/list/private")
    public Object privateList() {
        List<ChatList> result = chatListService.privateList();
        return Result.success(result);
    }

    @UrlLimit
    @ApiOperation("获取群聊列表")
    @GetMapping("/group")
    public Object group() {
        ChatList result = chatListService.getGroup();
        return Result.success(result);
    }

    @UrlLimit
    @ApiOperation("建立私聊对话")
    @PostMapping("/create")
    public Object create(@RequestBody @Valid CreateDTO createDTO) {
        ChatList result = chatListService.create(createDTO.getTargetId());
        return Result.success(result);
    }

    @UrlLimit
    @ApiOperation("标记消息已读")
    @PostMapping("/read")
    public Object read(@RequestBody @Valid ReadDTO readDTO) {
        boolean result = chatListService.read(readDTO.getTargetId());
        return Result.success(result);
    }

    @UrlLimit
    @ApiOperation("删除私聊对话")
    @PostMapping("/delete")
    public Object delete(@RequestBody @Valid DeleteDTO deleteDTO) {
        boolean result = chatListService.delete(deleteDTO.getChatListId());
        return Result.success(result);
    }

    @ApiOperation("新增群聊")
    @PostMapping("/group/create")
    public Object createGroup(@RequestBody @Valid CreateDTO createDTO) {
        ChatList result = chatListService.create(createDTO.getTargetId());
        return Result.success(result);
    }

}
