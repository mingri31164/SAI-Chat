package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.MinioUtil;
import com.mingri.core.toolkit.RedisUtils;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.core.toolkit.SecurityUtil;
import com.mingri.service.mail.VerificationCodeService;
import com.mingri.service.mail.repo.EmailVerifyByAccountVo;
import com.mingri.service.mail.repo.EmailVerifyVo;
import com.mingri.service.user.repo.dto.UserDto;
import com.mingri.service.user.repo.entity.User;
import com.mingri.service.user.repo.req.*;
import com.mingri.service.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/v1/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;

    @Resource
    VerificationCodeService verificationCodeService;

    /**
     * 用户查询
     */
    @PostMapping("/search")
    public JSONObject searchUser(@RequestBody SearchUserVo searchUserVo) {
        List<UserDto> result = userService.searchUser(searchUserVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取用户每项未读数
     */
    @GetMapping("/unread")
    public JSONObject unreadInfo(@Userid String userId) {
        HashMap result = userService.unreadInfo(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 邮箱验证码
     */
    @PostMapping("/email/verify")
    @UrlFree
    public JSONObject emailVerify(@RequestBody EmailVerifyVo emailVerifyVo) {
        verificationCodeService.emailVerificationCode(emailVerifyVo.getEmail());
        return ResultUtil.Succeed();
    }

    /**
     * 邮箱验证码(通过账号)
     */
    @PostMapping("/email/verify/by/account")
    @UrlFree
    public JSONObject emailVerifyByAccount(@RequestBody EmailVerifyByAccountVo emailVerifyByAccountVo) {
        userService.emailVerifyByAccount(emailVerifyByAccountVo.getAccount());
        return ResultUtil.Succeed();
    }

    /**
     * 用户注册
     */
    @UrlFree
    @PostMapping("/register")
    public JSONObject register(@RequestBody RegisterVo registerVo) {
        String decryptedPassword = SecurityUtil.decryptPassword(registerVo.getPassword());
        registerVo.setPassword(decryptedPassword);
        boolean result = userService.register(registerVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 忘记密码
     */
    @UrlFree
    @PostMapping("/forget")
    public JSONObject forget(@RequestBody ForgetVo forgetVo) {
        String decryptedPassword = SecurityUtil.decryptPassword(forgetVo.getPassword());
        forgetVo.setPassword(decryptedPassword);
        boolean result = userService.forget(forgetVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public JSONObject info(@Userid String userId) {
        UserDto result = userService.info(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 修改当前用户信息
     */
    @PostMapping("/update")
    public JSONObject update(@Userid String userId, @RequestBody UpdateVo updateVo) {
        boolean result = userService.updateUserInfo(userId, updateVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 修改密码
     */
    @PostMapping("/update/password")
    public JSONObject updateUserPassword(@Userid String userId, @RequestBody UpdatePasswordVo updateVo) {
        String decryptedPassword = SecurityUtil.decryptPassword(updateVo.getConfirmPassword());
        updateVo.setConfirmPassword(decryptedPassword);
        User user = userService.getById(userId);
        if (SecurityUtil.verifyPassword(updateVo.getOldPassword(), user.getPassword())) {
            boolean result = userService.updateUserInfo(userId, updateVo);
            return ResultUtil.ResultByFlag(result);
        } else return ResultUtil.ResultByFlag(false, "原密码错误~", 400);
    }

    @PostMapping(value = "/upload/portrait")
    public JSONObject upload(HttpServletRequest request,
                             @Userid String userId,
                             @RequestHeader("name") String name,
                             @RequestHeader("type") String type,
                             @RequestHeader("size") long size) throws IOException {
        String fileName = userId + "-portrait" + name.substring(name.lastIndexOf("."));
        String url = minioUtil.upload(request.getInputStream(), fileName, type, size);
        url += "?t=" + System.currentTimeMillis();
        userService.updateUserPortrait(userId, url);
        return ResultUtil.Succeed(url);
    }

    @PostMapping(value = "upload/portrait/form")
    public JSONObject uploadFrom(@Userid String userId,
                                 @RequestParam("name") String name,
                                 @RequestParam("type") String type,
                                 @RequestParam("size") long size,
                                 @RequestParam("file") MultipartFile file) throws IOException {
        String fileName = userId + "-portrait" + name.substring(name.lastIndexOf("."));
        String url = minioUtil.upload(file.getInputStream(), fileName, type, size);
        url += "?t=" + System.currentTimeMillis();
        userService.updateUserPortrait(userId, url);
        return ResultUtil.Succeed(url);
    }


}
