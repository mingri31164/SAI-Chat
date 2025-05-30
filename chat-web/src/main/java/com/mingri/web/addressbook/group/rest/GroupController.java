package com.mingri.web.addressbook.group.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.chat.repo.vo.group.dto.GroupListDto;
import com.mingri.service.chat.repo.vo.group.req.CreateGroupReq;
import com.mingri.service.chat.repo.vo.group.req.DeleteGroupReq;
import com.mingri.service.chat.repo.vo.group.req.UpdateGroupReq;
import com.mingri.service.chat.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@Tag(name = "好友分组接口")
@RequestMapping("/v1/api/group")
public class GroupController {

    @Resource
    GroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "创建好友分组")
    public JSONObject createGroup(@Userid String userId, @RequestBody CreateGroupReq createGroupReq) {
        boolean flag = groupService.createGroup(userId, createGroupReq);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/update")
    @Operation(summary = "修改分组名称")
    public JSONObject updateGroup(@Userid String userId, @RequestBody UpdateGroupReq updateGroupReq) {
        boolean flag = groupService.updateGroup(userId, updateGroupReq);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除好友分组")
    public JSONObject deleteGroup(@Userid String userId, @RequestBody DeleteGroupReq deleteGroupReq) {
        boolean flag = groupService.deleteGroup(userId, deleteGroupReq);
        return ResultUtil.ResultByFlag(flag);
    }

    /**
     * 获取好友分组
     */
    @GetMapping("/list")
    @Operation(summary = "获取好友分组")
    public JSONObject getList(@Userid String userId) {
        List<GroupListDto> result = groupService.getList(userId);
        return ResultUtil.Succeed(result);
    }
}

