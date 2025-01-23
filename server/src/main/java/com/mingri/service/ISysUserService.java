package com.mingri.service;

import com.mingri.dto.SysUserLoginDTO;
import com.mingri.dto.SysUserRegisterDTO;
import com.mingri.entity.LoginUser;
import com.mingri.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-22
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    LoginUser login(SysUserLoginDTO userLoginDTO);



    /**
     * 用户注册
     * @param userRegisterDTO
     */
    void register(SysUserRegisterDTO userRegisterDTO);

    /**
     * 用户退出
     */
    void logout();

}
