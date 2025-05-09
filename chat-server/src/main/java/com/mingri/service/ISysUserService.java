package com.mingri.service;

import com.mingri.dto.user.SysUpdateDTO;
import com.mingri.dto.user.SysUserLoginDTO;
import com.mingri.dto.user.SysUserRegisterDTO;
import com.mingri.entity.login.LoginUser;
import com.mingri.entity.sys.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.result.Result;
import com.mingri.vo.sys.SysUserInfoVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户登录
     */
    void afterLogin(SysUser sysUser, LoginUser loginUser);


    /**
     * 用户注册
     *
     * @param userRegisterDTO
     */
    void register(SysUserRegisterDTO userRegisterDTO);

    /**
     * 用户退出
     */
    void logout();

    SysUserInfoVO getUserById(String userId);

    List<SysUserInfoVO> listUser();

    List<String> onlineWeb();

    Map<String, SysUserInfoVO> listMapUser();

    void online(String userId);

    void offline(String userId);

    void deleteExpiredUsers(LocalDate expirationDate);

    void updateUserBadge(String id);

    void initBotUser();

    boolean updateUser(SysUpdateDTO sysUpdateDTO);
}
