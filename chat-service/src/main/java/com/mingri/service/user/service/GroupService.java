package com.mingri.service.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.chat.dto.GroupListDto;
import com.mingri.model.vo.chat.req.CreateGroupVo;
import com.mingri.model.vo.chat.req.DeleteGroupVo;
import com.mingri.model.vo.chat.req.UpdateGroupVo;
import com.mingri.service.chat.repo.entity.Group;

import java.util.List;

/**
 * 分组表 服务类
 */
public interface GroupService extends IService<Group> {

    List<Group> getGroupByUserId(String userId);

    boolean createGroup(String userId, CreateGroupVo createGroupVo);

    boolean updateGroup(String userId, UpdateGroupVo updateGroupVo);

    boolean deleteGroup(String userId, DeleteGroupVo deleteGroupVo);

    List<GroupListDto> getList(String userId);

    boolean IsExistGroupByUserId(String userId, String GroupId);
}
