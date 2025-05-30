package com.mingri.service.chat.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.chat.repo.vo.group.dto.GroupListDto;
import com.mingri.service.chat.repo.vo.group.entity.Group;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分组表 Mapper 接口
 */
public interface GroupMapper extends BaseMapper<Group> {

    @Select("SELECT `name` AS `label`,`id` AS `value` FROM `group` WHERE `user_id` = #{userId}")
    List<GroupListDto> getList(String userId);
}
