package com.mingri.service.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.vo.group.dto.GroupListDto;
import com.mingri.service.chat.repo.vo.group.req.CreateGroupReq;
import com.mingri.service.chat.repo.vo.group.req.DeleteGroupReq;
import com.mingri.service.chat.repo.vo.group.req.UpdateGroupReq;
import com.mingri.service.chat.repo.vo.group.entity.Group;

import java.util.List;

/**
 * 分组表 服务类
 */
public interface GroupService extends IService<Group> {

    List<Group> getGroupByUserId(String userId);

    boolean createGroup(String userId, CreateGroupReq createGroupReq);

    boolean updateGroup(String userId, UpdateGroupReq updateGroupReq);

    boolean deleteGroup(String userId, DeleteGroupReq deleteGroupReq);

    List<GroupListDto> getList(String userId);

    boolean IsExistGroupByUserId(String userId, String GroupId);
}
