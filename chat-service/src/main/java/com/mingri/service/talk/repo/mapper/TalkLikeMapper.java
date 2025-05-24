package com.mingri.service.talk.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.model.vo.talk.dto.LikeListDto;
import com.mingri.model.vo.talk.entity.TalkLike;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * 说说点赞 Mapper 接口
 */
public interface TalkLikeMapper extends BaseMapper<TalkLike> {

    @Select("SELECT tl.id, u.`name`, u.`portrait`, f.`remark`, u.`id` AS `user_id` " +
            "FROM `talk_like` AS tl " +
            "         LEFT JOIN `user` AS u ON u.`id` = tl.`user_id` " +
            "         LEFT JOIN `friend` AS f ON f.`user_id` = #{userId} AND f.`friend_id` = tl.`user_id` " +
            "WHERE tl.`talk_id` = #{talkId} ")
    List<LikeListDto> talkLikeList(String userId, String talkId);
}
