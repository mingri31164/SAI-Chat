package com.mingri.service.chat.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.chat.repo.dto.FriendDetailsDto;
import com.mingri.service.chat.repo.entity.Friend;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友表 Mapper 接口
 */
public interface FriendMapper extends BaseMapper<Friend> {

    @Select("SELECT u.*, f.`remark`, g.`name` AS `group_name`, f.`friend_id`, f.`is_concern` " +
            "FROM `friend` AS f " +
            "         LEFT JOIN `user` AS u ON f.`friend_id` = u.`id` " +
            "         LEFT JOIN `group` AS g ON f.`group_id` = g.`id` " +
            "WHERE f.`user_id` = #{userId} " +
            "  AND (u.`account` LIKE #{searchInfo} OR u.`name` LIKE #{searchInfo} OR f.`remark` LIKE #{searchInfo})")
    List<FriendDetailsDto> searchFriends(@Param("userId") String userId, @Param("searchInfo") String searchInfo);

}
