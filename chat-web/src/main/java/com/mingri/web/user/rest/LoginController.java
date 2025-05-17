package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.UserIp;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.core.toolkit.SecurityUtil;
import com.mingri.service.user.repo.req.login.LoginVo;
import com.mingri.service.user.repo.req.login.QrCodeLoginVo;
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
    public Object login(@Valid @RequestBody LoginVo loginVo, @UserIp String userIp) {
        String decryptedPassword = SecurityUtil.decryptPassword(loginVo.getPassword());
        loginVo.setPassword(decryptedPassword);
        JSONObject result = userService.validateLogin(loginVo, userIp, false);
        return result;
    }

    @PostMapping("/qr")
    public Object qrCodeLogin(@Valid @RequestBody QrCodeLoginVo qrCodeLoginVo, @Userid String userid) {
        JSONObject result = userService.validateQrCodeLogin(qrCodeLoginVo, userid);
        return result;
    }
}
