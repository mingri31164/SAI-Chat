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
    public JSONObject userList(@RequestBody UserListVo userListVo) {
        Page<User> result = userService.userList(userListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/create")
    @UrlResource("admin")
    public JSONObject createUser(@RequestBody CreateUserVo createUserVo) {
        boolean result = userService.createUser(createUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/update")
    @UrlResource("admin")
    public JSONObject updateUser(@RequestBody UpdateUserVo updateUserVo) {
        boolean result = userService.updateUser(updateUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/disable")
    @UrlResource("admin")
    public JSONObject disableUser(@Userid String userid, @RequestBody DisableUserVo disableUserVo) {
        boolean result = userService.disableUser(userid, disableUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/undisable")
    @UrlResource("admin")
    public JSONObject undisableUser(@RequestBody UndisableUserVo undisableUserVo) {
        boolean result = userService.undisableUser(undisableUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/delete")
    @UrlResource("admin")
    public JSONObject deleteUser(@Userid String userid, @RequestBody DeleteUserVo deleteUserVo) {
        boolean result = userService.deleteUser(userid, deleteUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/reset/password")
    @UrlResource("admin")
    public JSONObject restPassword(@RequestBody ResetPasswordVo resetPasswordVo) {
        boolean result = userService.restPassword(resetPasswordVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/set/admin")
    @UrlResource("admin")
    public JSONObject setAdmin(@Userid String userid, @RequestBody SetAdminVo setAdminVo) {
        boolean result = userService.setAdmin(userid, setAdminVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/cancel/admin")
    @UrlResource("admin")
    public JSONObject cancelAdmin(@Userid String userid, @RequestBody CancelAdminVo cancelAdminVo) {
        boolean result = userService.cancelAdmin(userid, cancelAdminVo);
        return ResultUtil.ResultByFlag(result);
    }
}
