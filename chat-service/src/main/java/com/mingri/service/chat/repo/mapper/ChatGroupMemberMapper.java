package com.mingri.service.chat.repo.mapper;

import com.mingri.service.chat.repo.entity.ChatGroupMemberDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 聊天群成员表 Mapper 接口
 * </p>
 *
 * @author mingri31164
 * @since 2025-05-03
 */
@Mapper
public interface ChatGroupMemberMapper extends BaseMapper<ChatGroupMemberDO> {

}
