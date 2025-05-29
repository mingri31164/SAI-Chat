package com.mingri.web.addressbook.friend.rest;


import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.core.toolkit.SecurityUtil;
import com.mingri.model.vo.chat.friend.req.*;
import com.mingri.model.vo.chat.friend.dto.FriendDetailsDto;
import com.mingri.model.vo.chat.friend.dto.FriendListDto;
import com.mingri.model.vo.chat.friend.entity.Friend;
import com.mingri.service.chat.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;


@RestController
@Tag(name = "好友基础接口")
@RequiredArgsConstructor
@RequestMapping("/v1/api/friend")
public class FriendController {

    @Resource
    FriendService friendService;

    private final MinioUtil minioUtil;

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取好友列表")
    public JSONObject getFriendList(@Userid String userId) {
        List<FriendListDto> friendListDto = friendService.getFriendList(userId);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list/flat")
    @Operation(summary = "获取好友列表（模糊查询）")
    public JSONObject getFriendListFlat(@Userid String userId, @RequestParam(defaultValue = "") String friendInfo) {
        List<Friend> friendListDto = friendService.getFriendListFlat(userId, friendInfo);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 判断是否是好友
     */
    @GetMapping("/is/friend")
    @Operation(summary = "判断是否是好友")
    public JSONObject isFriend(@Userid String userId, @RequestParam String targetId) {
        boolean result = friendService.isFriend(userId, targetId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取好友列表(未读消息数)
     */
    @GetMapping("/list/flat/unread")
    @Operation(summary = "获取好友列表(未读消息数)")
    public JSONObject getFriendListFlatUnread(@Userid String userId, @RequestParam(defaultValue = "") String friendInfo) {
        List<Friend> friendListDto = friendService.getFriendListFlatUnread(userId, friendInfo);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 获取好友详情
     */
    @GetMapping("/details/{friendId}")
    @Operation(summary = "获取好友详情")
    public JSONObject getFriendDetails(@Userid String userId, @PathVariable String friendId) {
        FriendDetailsDto friendDetailsDto = friendService.getFriendDetails(userId, friendId);
        return ResultUtil.Succeed(friendDetailsDto);
    }

    /**
     * 搜索好友
     */
    @PostMapping("/search")
    @Operation(summary = "搜索好友")
    public JSONObject searchFriends(@Userid String userId, @RequestBody SearchReq searchReq) {
        List<FriendDetailsDto> result = friendService.searchFriends(userId, searchReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 同意好友请求
     */
    @PostMapping("/agree")
    @Operation(summary = "同意好友请求（通知）")
    public JSONObject agreeFriendApply(@Userid String userId, @RequestBody AgreeFriendApplyReq agreeFriendApplyVo) {
        boolean result = friendService.agreeFriendApply(userId, agreeFriendApplyVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 同意好友请求
     */
    @PostMapping("/agree/id")
    @Operation(summary = "同意好友请求")
    public JSONObject agreeFriendApplyFromId(@Userid String userId, @RequestBody AgreeFriendApplyReq agreeFriendApplyVo) {
        boolean result = friendService.agreeFriendApply(userId, agreeFriendApplyVo.getFromId());
        return ResultUtil.Succeed(result);
    }

    /**
     * 拒绝好友请求
     */
    @PostMapping("/reject")
    @Operation(summary = "拒绝好友请求")
    public JSONObject refuseFriendApply(@Userid String userId, @RequestBody RejectFriendApplyReq friendApplyVo) {
        boolean result = friendService.rejectFriendApply(userId, friendApplyVo.getFromId());
        return ResultUtil.Succeed(result);
    }


    /**
     * 扫码好友请求
     */
    @PostMapping("/add/qr")
    @Operation(summary = "扫码好友请求")
    public JSONObject addFriendByQr(@Userid String userId, @RequestBody AddFriendByQrReq AddFriendByQrVo) {
        String targetId = SecurityUtil.aesDecrypt(AddFriendByQrVo.getQrCode());
        boolean result = friendService.addFriendApply(userId, targetId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置好友备注
     */
    @PostMapping("/set/remark")
    @Operation(summary = "设置好友备注")
    public JSONObject setRemark(@Userid String userId, @RequestBody SetRemarkReq setRemarkVo) {
        boolean result = friendService.setRemark(userId, setRemarkVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置好友分组
     */
    @PostMapping("/set/group")
    @Operation(summary = "设置好友分组")
    public JSONObject setGroup(@Userid String userId, @RequestBody SetGroupReq setGroupVo) {
        boolean result = friendService.setGroup(userId, setGroupVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除好友
     */
    @PostMapping("/delete")
    @Operation(summary = "删除好友")
    public JSONObject deleteFriend(@Userid String userId, @RequestBody DeleteFriendReq deleteFriendVo) {
        boolean result = friendService.deleteFriend(userId, deleteFriendVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 特别关心
     */
    @PostMapping("/carefor")
    @Operation(summary = "设置特别关心")
    public JSONObject careForFriend(@Userid String userId, @RequestBody CareForFriendReq careForFriendVo) {
        boolean result = friendService.careForFriend(userId, careForFriendVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 取消特别关心
     */
    @PostMapping("/uncarefor")
    @Operation(summary = "取消特别关心")
    public JSONObject unCareForFriend(@Userid String userId, @RequestBody UnCareForFriendReq unCareForFriendReq) {
        boolean result = friendService.unCareForFriend(userId, unCareForFriendReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 设置聊天背景
     */
    @PostMapping("/set-chat-background")
    @Operation(summary = "设置聊天背景")
    public JSONObject setChatBackground(@Userid String userId,
                                        @RequestParam("friendId") String friendId,
                                        @RequestParam("name") String name,
                                        @RequestParam("type") String type,
                                        @RequestParam("size") long size,
                                        @RequestParam("file") MultipartFile file) {

        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        Friend friend = friendService.getOne(queryWrapper);
        if (friend == null) return ResultUtil.Fail("好友不存在");
        boolean update;
        String url;
        try {
            String fileName = userId+"-"+friendId + "-chat-background" + name.substring(name.lastIndexOf("."));
            url = minioUtil.upload(file.getInputStream(), fileName, type, size);
            url += "?t=" + System.currentTimeMillis();
            friend.setChatBackground(url);
            update = friendService.updateById(friend);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!update) return ResultUtil.Fail("设置失败");
        return ResultUtil.Succeed(url);
    }


}
