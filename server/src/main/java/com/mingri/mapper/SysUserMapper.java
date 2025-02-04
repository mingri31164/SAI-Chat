package com.mingri.mapper;

import com.mingri.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.vo.SysUserInfoVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;



@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE id = #{userId}")
    @ResultMap("UserDtoResultMap")
    SysUserInfoVO getUserById(String userId);

    @Select("SELECT * FROM sys_user ORDER BY user_type DESC")
    @ResultMap("UserDtoResultMap")
    List<SysUserInfoVO> listUser();

    @Select("SELECT * FROM sys_user ORDER BY user_type DESC")
    @MapKey("id")
    @ResultMap("UserDtoResultMap")
    Map<String, SysUserInfoVO> listMapUser();

}
