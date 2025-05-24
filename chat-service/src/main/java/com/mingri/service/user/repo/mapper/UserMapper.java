package com.mingri.service.user.repo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.model.vo.user.dto.UserDto;
import com.mingri.model.vo.user.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户表 Mapper 接口
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from `user` where `id` = #{userId}")
    UserDto info(String userId);

    @Select("SELECT * FROM user WHERE account = #{userInfo} OR phone = #{userInfo} OR email = #{userInfo}")
    List<UserDto> findUserByInfo(String userInfo);
}
