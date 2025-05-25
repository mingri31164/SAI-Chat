package com.mingri.web.addressbook.group.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.chat.group.dto.GroupListDto;
import com.mingri.model.vo.chat.group.req.CreateGroupReq;
import com.mingri.model.vo.chat.group.req.DeleteGroupReq;
import com.mingri.model.vo.chat.group.req.UpdateGroupReq;
import com.mingri.service.chat.service.GroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/v1/api/group")
public class GroupController {

    @Resource
    GroupService groupService;

    @PostMapping("/create")
    public JSONObject createGroup(@Userid String userId, @RequestBody CreateGroupReq createGroupReq) {
        boolean flag = groupService.createGroup(userId, createGroupReq);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/update")
    public JSONObject updateGroup(@Userid String userId, @RequestBody UpdateGroupReq updateGroupReq) {
        boolean flag = groupService.updateGroup(userId, updateGroupReq);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/delete")
    public JSONObject deleteGroup(@Userid String userId, @RequestBody DeleteGroupReq deleteGroupReq) {
        boolean flag = groupService.deleteGroup(userId, deleteGroupReq);
        return ResultUtil.ResultByFlag(flag);
    }

    /**
     * 获取用户分组
     */
    @GetMapping("/list")
    public JSONObject getList(@Userid String userId) {
        List<GroupListDto> result = groupService.getList(userId);
        return ResultUtil.Succeed(result);
    }
}

