package com.mingri.service.user.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.user.dto.UserDto;
import com.mingri.model.vo.user.req.*;
import com.mingri.service.user.repo.entity.User;

import java.util.HashMap;
import java.util.List;

/**
 * 用户表 服务类
 */
public interface UserService extends IService<User> {

    List<UserDto> searchUser(SearchUserVo searchUserVo);

    HashMap<String, Integer> unreadInfo(String userId);

    UserDto info(String userId);

    boolean updateUserInfo(String userId, UpdateVo updateVo);

    boolean updateUserInfo(String userId, UpdatePasswordVo updateVo);

    boolean updateUserPortrait(String userId, String portrait);

    boolean register(RegisterVo registerVo);

    boolean forget(ForgetVo forgetVo);

    User getUserByAccount(String account);


    void emailVerifyByAccount(String account);
}
