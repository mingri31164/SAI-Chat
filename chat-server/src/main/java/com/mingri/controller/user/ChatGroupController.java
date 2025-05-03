package com.mingri.controller.user;


import cn.hutool.json.JSONObject;
import com.mingri.entity.ChatGroup;
import com.mingri.result.Result;
import com.mingri.service.IChatGroupService;
import com.mingri.vo.chatGroup.CreateChatGroupVo;
import com.mingri.vo.chatGroup.DissolveChatGroupVo;
import com.mingri.vo.chatGroup.UpdateChatGroupVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "群聊接口")
@RestController
@RequestMapping("/chat-group")
public class ChatGroupController {

    @Resource
    private IChatGroupService chatGroupService;


    /**
     * 聊天群列表
     */
    @ApiOperation("获取当前用户的群聊列表")
    @GetMapping("/list")
    public Object chatGroupList(String userId) {
        List<ChatGroup> result = chatGroupService.chatGroupList(userId);
        return Result.success(result);
    }

    /**
     * 创建聊天群
     */
    @ApiOperation("创建聊天群")
    @PostMapping("/create")
    public Object createChatGroup(String userId, @RequestBody CreateChatGroupVo createChatGroupVo) {
        boolean result = chatGroupService.createChatGroup(userId, createChatGroupVo);
        return Result.success(result);
    }

    /**
     * 更新群信息
     */
    @PostMapping("/update")
    public Object updateChatGroup(String userId, @RequestBody UpdateChatGroupVo updateChatGroupVo) {
        boolean result = chatGroupService.updateChatGroup(userId, updateChatGroupVo);
        return Result.success(result);
    }



}
