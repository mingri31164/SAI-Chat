package com.mingri.controller;


import com.mingri.annotation.UrlLimit;
import com.mingri.dto.chatList.CreateDTO;
import com.mingri.dto.chatList.DeleteDTO;
import com.mingri.dto.chatList.ReadDTO;
import com.mingri.entity.ChatList;
import com.mingri.result.Result;
import com.mingri.service.IChatListService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.lang.annotation.Target;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-24
 */

@Api(tags = "消息列表接口")
@RestController
@RequestMapping("/api/v1/chat-list")
public class ChatListController {

    @Resource
    IChatListService chatListService;

//    @UrlLimit
    @GetMapping("/list/private")
    public Object privateList() {
        List<ChatList> result = chatListService.privateList();
        return Result.success(result);
    }

//    @UrlLimit
    @GetMapping("/group")
    public Object group() {
        ChatList result = chatListService.getGroup();
        return Result.success(result);
    }

//    @UrlLimit
    @PostMapping("/create")
    public Object create(@RequestBody @Valid CreateDTO createDTO) {
        ChatList result = chatListService.create(createDTO.getTargetId());
        return Result.success(result);
    }

//    @UrlLimit
    @PostMapping("/read")
    public Object read(@RequestBody @Valid ReadDTO readDTO) {
        boolean result = chatListService.read(readDTO.getTargetId());
        return Result.success(result);
    }

//    @UrlLimit
    @PostMapping("/delete")
    public Object delete(@RequestBody @Valid DeleteDTO deleteDTO) {
        boolean result = chatListService.delete(deleteDTO.getChatListId());
        return Result.success(result);
    }

}
