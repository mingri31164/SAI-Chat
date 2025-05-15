package com.mingri.web.chat.chatgroup.rest;

import com.mingri.service.chat.repo.entity.ChatGroupDO;
import com.mingri.model.result.Result;
import com.mingri.service.chat.service.IChatGroupService;
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



}
