package com.mingri.web.chat.group.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.chat.dto.GroupListDto;
import com.mingri.model.vo.chat.req.CreateGroupVo;
import com.mingri.model.vo.chat.req.DeleteGroupVo;
import com.mingri.model.vo.chat.req.UpdateGroupVo;
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
    public JSONObject createGroup(@Userid String userId, @RequestBody CreateGroupVo createGroupVo) {
        boolean flag = groupService.createGroup(userId, createGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/update")
    public JSONObject updateGroup(@Userid String userId, @RequestBody UpdateGroupVo updateGroupVo) {
        boolean flag = groupService.updateGroup(userId, updateGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/delete")
    public JSONObject deleteGroup(@Userid String userId, @RequestBody DeleteGroupVo deleteGroupVo) {
        boolean flag = groupService.deleteGroup(userId, deleteGroupVo);
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

