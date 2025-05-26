package com.mingri.web.backend.rest;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mingri.core.annotation.UrlResource;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.admin.req.user.*;
import com.mingri.model.vo.user.entity.User;
import com.mingri.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("AdminUserController")
@RequestMapping("/admin/v1/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/page")
    @UrlResource("admin")
    public JSONObject userList(@RequestBody UserListReq userListReq) {
        Page<User> result = userService.userList(userListReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/create")
    @UrlResource("admin")
    public JSONObject createUser(@RequestBody CreateUserReq createUserReq) {
        boolean result = userService.createUser(createUserReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/update")
    @UrlResource("admin")
    public JSONObject updateUser(@RequestBody UpdateUserReq updateUserReq) {
        boolean result = userService.updateUser(updateUserReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/disable")
    @UrlResource("admin")
    public JSONObject disableUser(@Userid String userid, @RequestBody DisableUserReq disableUserReq) {
        boolean result = userService.disableUser(userid, disableUserReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/undisable")
    @UrlResource("admin")
    public JSONObject undisableUser(@RequestBody UndisableUserReq undisableUserReq) {
        boolean result = userService.undisableUser(undisableUserReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/delete")
    @UrlResource("admin")
    public JSONObject deleteUser(@Userid String userid, @RequestBody DeleteUserReq deleteUserReq) {
        boolean result = userService.deleteUser(userid, deleteUserReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/reset/password")
    @UrlResource("admin")
    public JSONObject restPassword(@RequestBody ResetPasswordReq resetPasswordReq) {
        boolean result = userService.restPassword(resetPasswordReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/set/admin")
    @UrlResource("admin")
    public JSONObject setAdmin(@Userid String userid, @RequestBody SetAdminReq setAdminReq) {
        boolean result = userService.setAdmin(userid, setAdminReq);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/cancel/admin")
    @UrlResource("admin")
    public JSONObject cancelAdmin(@Userid String userid, @RequestBody CancelAdminReq cancelAdminReq) {
        boolean result = userService.cancelAdmin(userid, cancelAdminReq);
        return ResultUtil.ResultByFlag(result);
    }
}
