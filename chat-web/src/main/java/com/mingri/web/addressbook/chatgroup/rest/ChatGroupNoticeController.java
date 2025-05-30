package com.mingri.web.addressbook.chatgroup.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroupNotice;
import com.mingri.service.chat.repo.vo.chatgroup.req.CreateNoticeReq;
import com.mingri.service.chat.repo.vo.chatgroup.req.DeleteNoticeReq;
import com.mingri.service.chat.repo.vo.chatgroup.req.NoticeListReq;
import com.mingri.service.chat.repo.vo.chatgroup.req.UpdateNoticeReq;
import com.mingri.service.chat.service.ChatGroupNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag(name = "聊天群通知接口")
@RequestMapping("/v1/api/chat-group-notice")
public class ChatGroupNoticeController {

    @Resource
    ChatGroupNoticeService chatGroupNoticeService;


    /**
     * 创建群公告
     */
    @PostMapping("/create")
    @Operation(summary = "创建群公告")
    public JSONObject createNotice(@Userid String userId, @RequestBody CreateNoticeReq createNoticeVo) {
        boolean result = chatGroupNoticeService.createNotice(userId, createNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 群公告列表
     */
    @PostMapping("/list")
    @Operation(summary = "获取群公告列表")
    public JSONObject noticeList(@Userid String userId, @RequestBody NoticeListReq noticeListVo) {
        List<ChatGroupNotice> result = chatGroupNoticeService.noticeList(userId, noticeListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除群公告
     */
    @PostMapping("/delete")
    @Operation(summary = "删除群公告")
    public JSONObject deleteNotice(@Userid String userId, @RequestBody DeleteNoticeReq deleteNoticeVo) {
        boolean result = chatGroupNoticeService.deleteNotice(userId, deleteNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 编辑群公告
     */
    @PostMapping("/update")
    @Operation(summary = "编辑群公告")
    public JSONObject updateNotice(@Userid String userId, @RequestBody UpdateNoticeReq updateNoticeVo) {
        boolean result = chatGroupNoticeService.updateNotice(userId, updateNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }
}
