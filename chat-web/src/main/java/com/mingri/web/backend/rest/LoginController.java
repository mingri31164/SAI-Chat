package com.mingri.web.backend.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.UserIp;
import com.mingri.core.toolkit.SecurityUtil;
import com.mingri.model.vo.user.req.login.LoginReq;
import com.mingri.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;


@RestController("AdminLoginController")
@Tag(name = "管理端-登录接口")
@RequestMapping("/admin/v1/api/login")
public class LoginController {

    @Resource
    UserService userService;

    @UrlFree
    @PostMapping()
    @Operation(summary = "管理员登录")
    public Object login(@Valid @RequestBody LoginReq loginVo, @UserIp String userIp) {
        String decryptedPassword = SecurityUtil.decryptPassword(loginVo.getPassword());
        loginVo.setPassword(decryptedPassword);
        JSONObject result = userService.validateLogin(loginVo, userIp, true);
        return result;
    }
}
