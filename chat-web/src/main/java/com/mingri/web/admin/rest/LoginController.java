package com.mingri.web.admin.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.UserIp;
import com.mingri.core.toolkit.SecurityUtil;
import com.mingri.model.vo.user.req.login.LoginReq;
import com.mingri.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController("AdminLoginController")
@RequestMapping("/admin/v1/api/login")
@Slf4j
public class LoginController {

    @Resource
    UserService userService;

    @UrlFree
    @PostMapping()
    public Object login(@Valid @RequestBody LoginReq loginVo, @UserIp String userIp) {
        String decryptedPassword = SecurityUtil.decryptPassword(loginVo.getPassword());
        loginVo.setPassword(decryptedPassword);
        JSONObject result = userService.validateLogin(loginVo, userIp, true);
        return result;
    }
}
