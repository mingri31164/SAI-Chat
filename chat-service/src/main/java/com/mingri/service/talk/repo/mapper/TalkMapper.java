package com.mingri.service.talk.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.model.vo.talk.dto.TalkListDto;
import com.mingri.model.vo.talk.entity.Talk;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 说说 Mapper 接口
 */
public interface TalkMapper extends BaseMapper<Talk> {

    @Select("<script>" +
            "SELECT  " +
            "    u.name, " +
            "    u.id, " +
            "    t.user_id, " +
            "    u.portrait, " +
            "    CASE  " +
            "        WHEN t.user_id =  #{userId} THEN NULL  " +
            "        ELSE f.remark  " +
            "    END AS remark, " +
            "    t.id AS talk_id, " +
            "    t.`latest_comment`, " +
            "    t.`create_time` AS `time`, " +
            "    t.content, " +
            "    t.like_num, " +
            "    t.comment_num " +
            "FROM  " +
            "    talk AS t " +
            "LEFT JOIN  " +
            "    friend AS f ON (t.user_id = f.friend_id AND f.user_id =  #{userId}) " +
            "LEFT JOIN  " +
            "    user AS u ON u.id = t.user_id " +
            "LEFT JOIN  " +
            "    talk_permission AS tp ON t.id = tp.talk_id " +
            "WHERE  " +
            "    (t.user_id =  #{userId}  " +
            "    OR (f.user_id =  #{userId} AND (tp.permission =  #{userId} OR tp.permission = 'all'))) " +
            "<if test='targetId != null and targetId != \"\"'>" +
            "    AND t.user_id = #{targetId} " +
            "</if>" +
            "ORDER BY  " +
            "    t.create_time DESC " +
            "LIMIT #{index}, #{num} " +
            "</script>")
    @ResultMap("TalkListDtoResultMap")
    List<TalkListDto> talkList(String userId, int index, int num, String targetId);


    @Select("SELECT  " +
            "    u.name, " +
            "    u.id, " +
            "    t.user_id, " +
            "    u.portrait, " +
            "    CASE  " +
            "        WHEN t.user_id =  #{userId} THEN NULL  " +
            "        ELSE f.remark  " +
            "    END AS remark, " +
            "    t.id AS talk_id, " +
            "    t.`latest_comment`, " +
            "    t.`create_time` AS `time`, " +
            "    t.content, " +
            "    t.like_num, " +
            "    t.comment_num " +
            "FROM  " +
            "    talk AS t " +
            "LEFT JOIN  " +
            "    friend AS f ON (t.user_id = f.friend_id AND f.user_id =  #{userId}) " +
            "LEFT JOIN  " +
            "    user AS u ON u.id = t.user_id " +
            "LEFT JOIN  " +
            "    talk_permission AS tp ON t.id = tp.talk_id " +
            "WHERE  " +
            "    t.id =  #{talkId} AND " +
            "    (t.user_id =  #{userId}  " +
            "    OR (f.user_id =  #{userId} AND (tp.permission =  #{userId} OR tp.permission = 'all'))) ")
    @ResultMap("TalkListDtoResultMap")
    TalkListDto detailsTalk(String userId, String talkId);

    @Select("SELECT  t.* " +
            "FROM  " +
            "    talk AS t " +
            "LEFT JOIN  " +
            "    talk_permission AS tp ON t.id = tp.talk_id " +
            "WHERE  " +
            "    t.user_id =  #{friendId} AND (tp.permission =  #{userId} OR tp.permission = 'all') " +
            "ORDER BY  " +
            "    t.create_time DESC " +
            "LIMIT 1 ")
    @ResultMap("mybatis-plus_Talk")
    Talk getLatestTalkContent(String userId, String friendId);
}
