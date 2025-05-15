package com.mingri.service.user.service;

import com.mingri.model.vo.sys.dto.SysUpdateDTO;
import com.mingri.model.vo.sys.dto.SysUserInfoDTO;
import com.mingri.model.vo.sys.dto.SysUserRegisterDTO;
import com.mingri.service.user.repo.entity.helper.LoginUser;
import com.mingri.service.user.repo.entity.SysUserDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


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
    void register(SysUserRegisterDTO userRegisterDTO);

    /**
     * 用户退出
     */
    void logout();

    SysUserInfoDTO getUserById(String userId);

    List<SysUserInfoDTO> listUser();

    List<String> onlineWeb();

    Map<String, SysUserInfoDTO> listMapUser();

    void online(String userId);

    void offline(String userId);

    void deleteExpiredUsers(LocalDate expirationDate);

    void updateUserBadge(String id);

    void initBotUser();

    boolean updateUser(SysUpdateDTO sysUpdateDTO);
}
