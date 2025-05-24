package com.mingri.service.user.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.user.req.*;
import com.mingri.model.vo.user.dto.UserDto;
import com.mingri.model.vo.user.req.login.LoginReq;
import com.mingri.model.vo.user.req.login.QrCodeLoginReq;
import com.mingri.model.vo.user.entity.User;

import java.util.HashMap;
import java.util.List;

/**
 * 用户表 服务类
 */
public interface UserService extends IService<User> {

    List<UserDto> searchUser(SearchUserReq searchUserReq);

    HashMap<String, Integer> unreadInfo(String userId);

    UserDto info(String userId);

    boolean updateUserInfo(String userId, UpdateReq updateReq);

    boolean updateUserInfo(String userId, UpdatePasswordReq updateVo);

    boolean updateUserPortrait(String userId, String portrait);

    boolean register(RegisterReq registerReq);

    boolean forget(ForgetReq forgetReq);

    User getUserByAccount(String account);

    void emailVerifyByAccount(String account);

    JSONObject validateLogin(LoginReq loginReq, String userIp, boolean b);

    JSONObject validateQrCodeLogin(QrCodeLoginReq qrCodeLoginReq, String userid);

    void online(String userId);

    void offline(String userId);

    List<User> getUserByEmail(String email);
}
