package com.mingri.service.user.repo.mapper;

import com.mingri.model.vo.sys.dto.SysUserInfoDTO;
import com.mingri.service.user.repo.entity.SysUserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;



@Mapper
public interface SysUserMapper extends BaseMapper<SysUserDO> {

    @Select("SELECT * FROM sys_user WHERE id = #{userId}")
    @ResultMap("UserDtoResultMap")
    SysUserInfoDTO getUserById(String userId);

    @Select("SELECT * FROM sys_user ORDER BY user_type DESC")
    @ResultMap("UserDtoResultMap")
    List<SysUserInfoDTO> listUser();

    @Select("SELECT * FROM sys_user ORDER BY user_type DESC")
    @MapKey("id")
    @ResultMap("UserDtoResultMap")
    Map<String, SysUserInfoDTO> listMapUser();

}
