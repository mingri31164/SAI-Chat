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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/v1/api/friend")
@RequiredArgsConstructor
public class FriendController {

    @Resource
    FriendService friendService;

    private final MinioUtil minioUtil;

    /**
     * 获取好友列表
     *
     * @return
     */
    @GetMapping("/list")
    public JSONObject getFriendList(@Userid String userId) {
        List<FriendListDto> friendListDto = friendService.getFriendList(userId);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 获取好友列表
     *
     * @return
     */
    @GetMapping("/list/flat")
    public JSONObject getFriendListFlat(@Userid String userId, @RequestParam(defaultValue = "") String friendInfo) {
        List<Friend> friendListDto = friendService.getFriendListFlat(userId, friendInfo);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 判断是否是好友
     */
    @GetMapping("/is/friend")
    public JSONObject isFriend(@Userid String userId, @RequestParam String targetId) {
        boolean result = friendService.isFriend(userId, targetId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取好友列表(未读消息数)
     *
     * @return
     */
    @GetMapping("/list/flat/unread")
    public JSONObject getFriendListFlatUnread(@Userid String userId, @RequestParam(defaultValue = "") String friendInfo) {
        List<Friend> friendListDto = friendService.getFriendListFlatUnread(userId, friendInfo);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 获取好友详情
     *
     * @return
     */
    @GetMapping("/details/{friendId}")
    public JSONObject getFriendDetails(@Userid String userId, @PathVariable String friendId) {
        FriendDetailsDto friendDetailsDto = friendService.getFriendDetails(userId, friendId);
        return ResultUtil.Succeed(friendDetailsDto);
    }

    /**
     * 搜索好友
     *
     * @return
     */
    @PostMapping("/search")
    public JSONObject searchFriends(@Userid String userId, @RequestBody SearchReq searchVo) {
        List<FriendDetailsDto> result = friendService.searchFriends(userId, searchVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 同意好友请求
     *
     * @return
     */
    @PostMapping("/agree")
    public JSONObject agreeFriendApply(@Userid String userId, @RequestBody AgreeFriendApplyReq agreeFriendApplyVo) {
        boolean result = friendService.agreeFriendApply(userId, agreeFriendApplyVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 同意好友请求
     *
     * @return
     */
    @PostMapping("/agree/id")
    public JSONObject agreeFriendApplyFromId(@Userid String userId, @RequestBody AgreeFriendApplyReq agreeFriendApplyVo) {
        boolean result = friendService.agreeFriendApply(userId, agreeFriendApplyVo.getFromId());
        return ResultUtil.Succeed(result);
    }

    /**
     * 拒绝好友请求
     *
     * @return
     */
    @PostMapping("/reject")
    public JSONObject refuseFriendApply(@Userid String userId, @RequestBody RejectFriendApplyReq friendApplyVo) {
        boolean result = friendService.rejectFriendApply(userId, friendApplyVo.getFromId());
        return ResultUtil.Succeed(result);
    }


    /**
     * 扫码好友请求
     *
     * @return
     */
    @PostMapping("/add/qr")
    public JSONObject addFriendByQr(@Userid String userId, @RequestBody AddFriendByQrReq AddFriendByQrVo) {
        String targetId = SecurityUtil.aesDecrypt(AddFriendByQrVo.getQrCode());
        boolean result = friendService.addFriendApply(userId, targetId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置好友备注
     *
     * @return
     */
    @PostMapping("/set/remark")
    public JSONObject setRemark(@Userid String userId, @RequestBody SetRemarkReq setRemarkVo) {
        boolean result = friendService.setRemark(userId, setRemarkVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置好友分组
     *
     * @return
     */
    @PostMapping("/set/group")
    public JSONObject setGroup(@Userid String userId, @RequestBody SetGroupReq setGroupVo) {
        boolean result = friendService.setGroup(userId, setGroupVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除好友
     *
     * @return
     */
    @PostMapping("/delete")
    public JSONObject deleteFriend(@Userid String userId, @RequestBody DeleteFriendReq deleteFriendVo) {
        boolean result = friendService.deleteFriend(userId, deleteFriendVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 特别关心
     *
     * @return
     */
    @PostMapping("/carefor")
    public JSONObject careForFriend(@Userid String userId, @RequestBody CareForFriendReq careForFriendVo) {
        boolean result = friendService.careForFriend(userId, careForFriendVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 特别关心
     *
     * @return
     */
    @PostMapping("/uncarefor")
    public JSONObject unCareForFriend(@Userid String userId, @RequestBody UnCareForFriendReq unCareForFriendReq) {
        boolean result = friendService.unCareForFriend(userId, unCareForFriendReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 设置聊天背景
     *
     * @return
     */
    @PostMapping("/set-chat-background")
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
