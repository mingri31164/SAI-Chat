package com.mingri.service.user.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.user.repo.vo.dto.UserDto;
import com.mingri.service.user.repo.vo.req.*;
import com.mingri.service.user.repo.vo.req.login.LoginReq;
import com.mingri.service.user.repo.vo.req.login.QrCodeLoginReq;
import com.mingri.service.user.repo.vo.entity.User;
import com.mingri.service.admin.repo.vo.req.user.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

/**
 * 用户表 服务类
 */
public interface UserService extends IService<User> {

    // 客户端

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

    String createThirdPartyUser(MultipartFile portrait, String name);

    boolean updateThirdPartyUser(MultipartFile portrait, String name, String userId);

    boolean deleteThirdPartyUser(String userId);

    boolean allUserOffline();




    // 管理端
    Page<User> userList(UserListReq userListReq);

    boolean createUser(CreateUserReq createUserReq);

    boolean updateUser(UpdateUserReq updateUserReq);

    boolean disableUser(String userid, DisableUserReq disableUserReq);

    boolean undisableUser(UndisableUserReq undisableUserReq);

    boolean deleteUser(String userid, DeleteUserReq deleteUserReq);

    boolean restPassword(ResetPasswordReq resetPasswordReq);

    boolean setAdmin(String userid, SetAdminReq setAdminReq);

    boolean cancelAdmin(String userid, CancelAdminReq cancelAdminReq);
}
