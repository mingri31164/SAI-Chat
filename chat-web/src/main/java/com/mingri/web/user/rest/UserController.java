package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.permission.UrlFree;
import com.mingri.core.argument.UserRole;
import com.mingri.core.argument.Userid;
import com.mingri.core.minio.MinioUtil;
import com.mingri.core.toolkit.RedisUtils;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.core.permission.SecurityUtil;
import com.mingri.model.exception.BaseException;
import com.mingri.service.chat.service.FriendService;
import com.mingri.service.mail.VerificationCodeService;
import com.mingri.service.mail.repo.EmailVerifyByAccountReq;
import com.mingri.service.mail.repo.EmailVerifyReq;
import com.mingri.service.user.repo.vo.dto.UserDto;
import com.mingri.service.user.repo.vo.entity.User;
import com.mingri.service.user.repo.vo.req.*;
import com.mingri.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;


@RestController
@Tag(name = "用户基础接口")
@RequestMapping("/v1/api/user")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;

    @Resource
    VerificationCodeService verificationCodeService;

    @Resource
    FriendService friendService;

    /**
     * 用户查询
     */
    @PostMapping("/search")
    @Operation(summary = "查询用户")
    public JSONObject searchUser(@RequestBody SearchUserReq searchUserReq) {
        List<UserDto> result = userService.searchUser(searchUserReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取用户每项未读数
     */
    @GetMapping("/unread")
    @Operation(summary = "获取用户每项未读数")
    public JSONObject unreadInfo(@Userid String userId) {
        HashMap result = userService.unreadInfo(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 邮箱验证码
     */
    @PostMapping("/email/verify")
    @Operation(summary = "获取邮箱验证码")
    @UrlFree
    public JSONObject emailVerify(@RequestBody EmailVerifyReq emailVerifyReq) {
        verificationCodeService.emailVerificationCode(emailVerifyReq.getEmail());
        return ResultUtil.Succeed();
    }

    /**
     * 邮箱验证码(通过账号)
     */
    @PostMapping("/email/verify/by/account")
    @Operation(summary = "获取邮箱验证码（通过账号）")
    @UrlFree
    public JSONObject emailVerifyByAccount(@RequestBody EmailVerifyByAccountReq emailVerifyByAccountReq) {
        userService.emailVerifyByAccount(emailVerifyByAccountReq.getAccount());
        return ResultUtil.Succeed();
    }

    /**
     * 用户注册
     */
    @UrlFree
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public JSONObject register(@RequestBody RegisterReq registerReq) {
        String decryptedPassword = SecurityUtil.decryptPassword(registerReq.getPassword());
        registerReq.setPassword(decryptedPassword);
        boolean result = userService.register(registerReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 忘记密码
     */
    @UrlFree
    @PostMapping("/forget")
    @Operation(summary = "忘记密码")
    public JSONObject forget(@RequestBody ForgetReq forgetReq) {
        String decryptedPassword = SecurityUtil.decryptPassword(forgetReq.getPassword());
        forgetReq.setPassword(decryptedPassword);
        boolean result = userService.forget(forgetReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "忘记密码")
    public JSONObject info(@Userid String userId) {
        UserDto result = userService.info(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 修改当前用户信息
     */
    @PostMapping("/update")
    @Operation(summary = "修改当前用户信息")
    public JSONObject update(@Userid String userId, @RequestBody UpdateReq updateReq) {
        boolean result = userService.updateUserInfo(userId, updateReq);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 修改密码
     */
    @PostMapping("/update/password")
    @Operation(summary = "修改密码")
    public JSONObject updateUserPassword(@Userid String userId, @RequestBody UpdatePasswordReq updateVo) {
        String decryptedPassword = SecurityUtil.decryptPassword(updateVo.getConfirmPassword());
        updateVo.setConfirmPassword(decryptedPassword);
        User user = userService.getById(userId);
        if (SecurityUtil.verifyPassword(updateVo.getOldPassword(), user.getPassword())) {
            boolean result = userService.updateUserInfo(userId, updateVo);
            return ResultUtil.ResultByFlag(result);
        } else return ResultUtil.ResultByFlag(false, "原密码错误~", 400);
    }

    @PostMapping(value = "/upload/portrait")
    @Operation(summary = "修改用户头像")
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
    @Operation(summary = "修改用户头像（表单）")
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


    /**
     * 获取文件
     */
    @GetMapping("/get/file")
    @Operation(summary = "获取文件")
    public ResponseEntity<InputStreamResource> getFile(@Userid String userId,
                                                       @UserRole String role,
                                                       @RequestHeader("targetId") String targetId,
                                                       @RequestHeader("fileName") String fileName) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, targetId);
        if (!isFriend && !userId.equals(targetId)
                && com.mingri.model.constant.UserRole.User.equals(role)) {
            throw new BaseException("双方非好友");
        }
        InputStream inputStream = minioUtil.getObject(targetId + "/img/" + fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    /**
     * 获取图片内容
     */
    @GetMapping("/get/img")
    @Operation(summary = "获取图片内容")
    public JSONObject getImg(@Userid String userId,
                             @UserRole String role,
                             @RequestParam("targetId") String targetId,
                             @RequestParam("fileName") String fileName) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, targetId);
        if (!isFriend && !userId.equals(targetId) &&
                com.mingri.model.constant.UserRole.User.equals(role)) {
            throw new BaseException("双方非好友");
        }
        String name = targetId + "/img/" + fileName;
        String url = (String) redisUtils.get(name);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.previewFile(name);
            redisUtils.set(name, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }


}
