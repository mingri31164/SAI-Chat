package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.UserIp;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.core.toolkit.SecurityUtil;
import com.mingri.model.vo.user.req.login.LoginReq;
import com.mingri.model.vo.user.req.login.QrCodeLoginReq;
import com.mingri.service.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;


@RestController
@RequestMapping("/v1/api/login")
@Slf4j
@AllArgsConstructor
public class LoginController {
    @Resource
    UserService userService;

    @UrlFree
    @GetMapping("/public-key")
    public Object getPublicKey() {
        String result = SecurityUtil.getPublicKey();
        return ResultUtil.Succeed(result);
    }

    @UrlFree
    @PostMapping()
    public Object login(@Valid @RequestBody LoginReq loginReq, @UserIp String userIp) {
        String decryptedPassword = SecurityUtil.decryptPassword(loginReq.getPassword());
        loginReq.setPassword(decryptedPassword);
        JSONObject result = userService.validateLogin(loginReq, userIp, false);
        return result;
    }

    @PostMapping("/qr")
    public Object qrCodeLogin(@Valid @RequestBody QrCodeLoginReq qrCodeLoginReq, @Userid String userid) {
        JSONObject result = userService.validateQrCodeLogin(qrCodeLoginReq, userid);
        return result;
    }
}
