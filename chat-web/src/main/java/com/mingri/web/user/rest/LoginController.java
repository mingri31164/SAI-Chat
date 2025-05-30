package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.permission.UrlFree;
import com.mingri.core.argument.UserIp;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.core.permission.SecurityUtil;
import com.mingri.service.user.repo.vo.req.login.LoginReq;
import com.mingri.service.user.repo.vo.req.login.QrCodeLoginReq;
import com.mingri.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;


@RestController
@Tag(name = "用户登录接口")
@AllArgsConstructor
@RequestMapping("/v1/api/login")
public class LoginController {
    @Resource
    UserService userService;

    @UrlFree
    @GetMapping("/public-key")
    @Operation(summary = "获取登录密钥")
    public Object getPublicKey() {
        String result = SecurityUtil.getPublicKey();
        return ResultUtil.Succeed(result);
    }

    @UrlFree
    @PostMapping()
    @Operation(summary = "用户登录")
    public Object login(@Valid @RequestBody LoginReq loginReq, @UserIp String userIp) {
        String decryptedPassword = SecurityUtil.decryptPassword(loginReq.getPassword());
        loginReq.setPassword(decryptedPassword);
        JSONObject result = userService.validateLogin(loginReq, userIp, false);
        return result;
    }

    @PostMapping("/qr")
    @Operation(summary = "二维码登录")
    public Object qrCodeLogin(@Valid @RequestBody QrCodeLoginReq qrCodeLoginReq, @Userid String userid) {
        JSONObject result = userService.validateQrCodeLogin(qrCodeLoginReq, userid);
        return result;
    }
}
