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

    @Select("SELECT f.*, u.`name` AS `name`, u.`portrait` AS portrait FROM `friend` AS f " +
            "JOIN `user` AS u ON f.`friend_id` = u.`id` " +
            "WHERE f.`user_id` = #{userId} AND f.`group_id`= #{groupId}")
    List<Friend> getFriendByUserIdAndGroupId(@Param("userId") String userId, @Param("groupId") String groupId);

    @Select("SELECT u.*, f.`remark`, g.`name` AS `group_name`, f.`friend_id`, f.`is_concern` " +
            "FROM `friend` AS f " +
            "         LEFT JOIN `user` AS u ON f.`friend_id` = u.`id` " +
            "         LEFT JOIN `group` AS g ON f.`group_id` = g.`id` " +
            "WHERE f.`user_id` = #{userId} " +
            "  AND f.`friend_id` = #{friendId} ")
    FriendDetailsDto getFriendDetails(@Param("userId") String userId, @Param("friendId") String friendId);

    @Select("SELECT f.*, u.`name` AS `name`, u.`portrait` AS portrait FROM `friend` AS f " +
            "JOIN `user` AS u ON f.`friend_id` = u.`id` " +
            "WHERE f.`user_id` = #{userId} AND f.`is_concern`= 1")
    List<Friend> getConcernFriendByUser(String userId);

    @Select("<script>" +
            "SELECT f.*, u.`name` AS `name`, u.`portrait` AS portrait, u.`account` AS account " +
            "FROM `friend` AS f " +
            "JOIN `user` AS u ON f.`friend_id` = u.`id` " +
            "WHERE f.`user_id` = #{userId} " +
            "<if test='friendInfo != null and friendInfo != \"\"'>" +
            "AND (u.`name` LIKE CONCAT('%', #{friendInfo}, '%') " +
            "OR u.`account` LIKE CONCAT('%', #{friendInfo}, '%') " +
            "OR f.`remark` LIKE CONCAT('%', #{friendInfo}, '%')) " +
            "</if>" +
            "</script>")
    List<Friend> getFriendListFlat(String userId, String friendInfo);

    @Select("<script>" +
            "SELECT f.*, u.`name` AS `name`, u.`portrait` AS portrait, u.`account` AS account, c.`unread_num`  " +
            "FROM `friend` AS f " +
            "LEFT JOIN `user` AS u ON f.`friend_id` = u.`id` " +
            "LEFT JOIN `chat_list` c ON f.`friend_id` = c.`from_id` AND c.`user_id` = #{userId} " +
            "WHERE f.`user_id` = #{userId} " +
            "<if test='friendInfo != null and friendInfo != \"\"'>" +
            "AND (u.`name` LIKE CONCAT('%', #{friendInfo}, '%') " +
            "OR u.`account` LIKE CONCAT('%', #{friendInfo}, '%') " +
            "OR f.`remark` LIKE CONCAT('%', #{friendInfo}, '%')) " +
            "</if>" +
            "ORDER BY c.`unread_num` desc " +
            "</script>")
    List<Friend> getFriendListFlatUnread(String userId, String friendInfo);


}
