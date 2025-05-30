package com.mingri.service.chat.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.service.chat.repo.vo.chatgroup.entity.ChatGroupNotice;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天群公告表 Mapper 接口
 */
public interface ChatGroupNoticeMapper extends BaseMapper<ChatGroupNotice> {


    @Select("select cgn.*, u.`name`, u.`portrait` " +
            "from `chat_group_notice` as cgn " +
            "left join `user` as u on u.`id` = cgn.`user_id` " +
            "where  cgn.`chat_group_id` = #{groupId} " +
            "order by cgn.`create_time` desc")
    List<ChatGroupNotice> noticeList(String groupId);
}
