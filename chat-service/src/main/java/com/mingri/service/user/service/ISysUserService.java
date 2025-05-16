package com.mingri.service.user.service;

import com.mingri.model.vo.sys.req.SysUserRegisterReq;
import com.mingri.service.user.repo.entity.helper.LoginUser;
import com.mingri.service.user.repo.entity.SysUserDO;
import com.baomidou.mybatisplus.extension.service.IService;


public interface ISysUserService extends IService<SysUserDO> {

    /**
     * 用户登录
     */
    void afterLogin(SysUserDO sysUser, LoginUser loginUser);


    /**
     * 用户注册
     *
     * @param userRegisterDTO
     */
    void register(SysUserRegisterReq userRegisterDTO);

    /**
     * 用户退出
     */
    void logout();

}
