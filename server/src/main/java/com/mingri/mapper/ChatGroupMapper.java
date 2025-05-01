package com.mingri.mapper;

import com.mingri.entity.ChatGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

}
