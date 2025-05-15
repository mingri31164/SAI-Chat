package com.mingri.service.admin.repo.mapper;

import com.mingri.service.admin.repo.entity.SysMenuDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuDO> {

    List<String> selectPermsByUserId(String id);

}
