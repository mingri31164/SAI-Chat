package com.mingri.web.chatgroup.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.entity.ChatGroupNotice;
import com.mingri.service.chat.repo.req.chatGroupNotice.CreateNoticeVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.DeleteNoticeVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.NoticeListVo;
import com.mingri.service.chat.repo.req.chatGroupNotice.UpdateNoticeVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/v1/api/chat-group-notice")
public class ChatGroupNoticeController {

    @Resource
    ChatGroupNoticeService chatGroupNoticeService;


    /**
     * 创建群公告
     *
     * @return
     */
    @PostMapping("/create")
    public JSONObject createNotice(@Userid String userId, @RequestBody CreateNoticeVo createNoticeVo) {
        boolean result = chatGroupNoticeService.createNotice(userId, createNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 群公告列表
     *
     * @return
     */
    @PostMapping("/list")
    public JSONObject noticeList(@Userid String userId, @RequestBody NoticeListVo noticeListVo) {
        List<ChatGroupNotice> result = chatGroupNoticeService.noticeList(userId, noticeListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除群公告
     *
     * @return
     */
    @PostMapping("/delete")
    public JSONObject deleteNotice(@Userid String userId, @RequestBody DeleteNoticeVo deleteNoticeVo) {
        boolean result = chatGroupNoticeService.deleteNotice(userId, deleteNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 编辑群公告
     *
     * @return
     */
    @PostMapping("/update")
    public JSONObject updateNotice(@Userid String userId, @RequestBody UpdateNoticeVo updateNoticeVo) {
        boolean result = chatGroupNoticeService.updateNotice(userId, updateNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }
}
