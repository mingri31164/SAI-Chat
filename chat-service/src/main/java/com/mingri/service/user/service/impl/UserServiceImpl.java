package com.mingri.service.user.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.core.config.MinioConfig;
import com.mingri.core.toolkit.*;
import com.mingri.model.constant.NotifyType;
import com.mingri.model.constant.UserRole;
import com.mingri.model.constant.UserStatus;
import com.mingri.model.exception.BaseException;
import com.mingri.service.user.repo.dto.UserDto;
import com.mingri.service.user.repo.dto.login.QrCodeResult;
import com.mingri.service.user.repo.req.*;
import com.mingri.service.user.repo.req.login.LoginVo;
import com.mingri.service.user.repo.req.login.QrCodeLoginVo;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.notify.service.NotifyService;
import com.mingri.service.mail.VerificationCodeService;
import com.mingri.service.user.repo.entity.User;
import com.mingri.service.user.repo.mapper.UserMapper;
import com.mingri.service.user.service.UserOperatedService;
import com.mingri.service.user.service.UserService;
import com.mingri.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 用户表 服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    ChatListService chatListService;
    @Resource
    NotifyService notifyService;
    @Resource
    UserMapper userMapper;
    @Resource
    MinioConfig minioConfig;
    @Resource
    RedisUtils redisUtils;
    @Resource
    VerificationCodeService verificationCodeService;
    @Resource
    UserOperatedService userOperatedService;
    @Resource
    WebSocketService webSocketService;


    @Override
    public List<UserDto> searchUser(SearchUserVo searchUserVo) {
        List<UserDto> users = userMapper.findUserByInfo(searchUserVo.getUserInfo());
        return users;
    }

    @Override
    public HashMap<String, Integer> unreadInfo(String userId) {
        HashMap<String, Integer> unreadInfo = new HashMap<>();
        //获取消息未读数
        int msgNum = chatListService.unread(userId);
        //获取通知未读数
        int notifyNum = notifyService.unread(userId);
        unreadInfo.put("chat", msgNum);
        unreadInfo.put("notify", notifyNum);
        unreadInfo.put("friendNotify", notifyService.unreadByType(userId, NotifyType.Friend_Apply));
        unreadInfo.put("systemNotify", notifyService.unreadByType(userId, NotifyType.System));
        return unreadInfo;
    }

    @Override
    public UserDto info(String userId) {
        UserDto user = userMapper.info(userId);
        return user;
    }

    @Override
    public boolean updateUserInfo(String userId, UpdateVo updateVo) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getName, updateVo.getName())
                .set(User::getPortrait, updateVo.getPortrait())
                .set(User::getSex, updateVo.getSex())
                .set(User::getBirthday, updateVo.getBirthday())
                .set(User::getSignature, updateVo.getSignature())
                .eq(User::getId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean updateUserInfo(String userId, UpdatePasswordVo updateVo) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        String passwordHash = SecurityUtil.hashPassword(updateVo.getConfirmPassword());
        updateWrapper.set(User::getPassword, passwordHash)
                .eq(User::getId, userId);
        return update(updateWrapper);
    }


    @Override
    public boolean updateUserPortrait(String userId, String portrait) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getPortrait, portrait)
                .eq(User::getId, userId);
        return update(updateWrapper);
    }


    @Override
    public boolean register(RegisterVo registerVo) {
        //验证码校验
        String code = (String) redisUtils.get(registerVo.getEmail());
        if (code == null || !code.equals(registerVo.getCode())) {
            throw new BaseException("验证码错误或者已失效~");
        }
        redisUtils.del(registerVo.getEmail());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, registerVo.getAccount());
        if (count(queryWrapper) > 0) {
            throw new BaseException("账号已存在~");
        }
        User user = new User();
        user.setId(IdUtil.randomUUID());
        user.setName(registerVo.getUsername());
        user.setAccount(registerVo.getAccount());
        String passwordHash = SecurityUtil.hashPassword(registerVo.getPassword());
        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
        user.setBirthday(new Date());
        user.setSex("男");
        user.setEmail(registerVo.getEmail());
        user.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-portrait.jpg");
        return save(user);
    }

    @Override
    public boolean forget(ForgetVo forgetVo) {
        User user = getUserByAccount(forgetVo.getAccount());
        if (null == user) throw new BaseException("用户不存在~");
        //验证码校验
        String code = (String) redisUtils.get(user.getEmail());
        if (code == null || !code.equals(forgetVo.getCode())) {
            throw new BaseException("验证码错误或者已失效~");
        }
        redisUtils.del(user.getEmail());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, forgetVo.getAccount())
                .eq(User::getEmail, user.getEmail());
//        User user = new User();
//        user.setId(IdUtil.randomUUID());
//        user.setAccount(forgetVo.getAccount());
        String passwordHash = SecurityUtil.hashPassword(forgetVo.getPassword());
//        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
//        user.setBirthday(new Date());
//        user.setSex("男");
//        user.setEmail(forgetVo.getEmail());
//        user.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-portrait.jpg");
        return updateById(user);
    }

    @Override
    public User getUserByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        return getOne(queryWrapper);
    }

    @Override
    public void emailVerifyByAccount(String account) {
        User user = getUserByAccount(account);
        if (null == user) {
            throw new BaseException("用户不存在~");
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            throw new BaseException("用户没有对应的邮箱~");
        }
        verificationCodeService.emailVerificationCode(user.getEmail());
    }

    @Override
    public JSONObject validateLogin(LoginVo loginVo, String userIp, boolean isAdmin) {
        // 获取用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(User::getAccount, loginVo.getAccount());
        User user = getOne(queryWrapper);
        if (null == user) {
            return ResultUtil.Fail("用户名或密码错误~");
        }
        if (!SecurityUtil.verifyPassword(loginVo.getPassword(), user.getPassword())) {
            return ResultUtil.Fail("用户名或密码错误~");
        }
        if (isAdmin && !UserRole.Admin.equals(user.getRole())) {
            return ResultUtil.Fail("您非管理员~");
        }
        JSONObject userinfo = createUserToken(user, userIp);
        user.setOnlineEquipment(loginVo.getOnlineEquipment());
        boolean isSave = updateById(user);
        return isSave?ResultUtil.Succeed(userinfo): ResultUtil.Fail("登录失败~");
    }

    @Override
    public JSONObject validateQrCodeLogin(QrCodeLoginVo qrCodeLoginVo, String userid) {
        // 获取用户
        User user = getById(userid);
        if (null == user) {
            return ResultUtil.Fail("用户不存在~");
        }
        String result = (String) redisUtils.get(qrCodeLoginVo.getKey());
        if (null == result) {
            return ResultUtil.Fail("二维码已失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        JSONObject userinfo = createUserToken(user, qrCodeResult.getIp());
        qrCodeResult.setStatus("success");
        qrCodeResult.setExtend(userinfo);
        redisUtils.set(qrCodeLoginVo.getKey(), JSONUtil.toJsonStr(qrCodeResult), 60);
        return ResultUtil.Succeed();
    }

    @Override
    public void online(String userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getIsOnline, true)
                .eq(User::getId, userId);
        update(updateWrapper);
    }

    @Override
    public void offline(String userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getIsOnline, false)
                .set(User::getOnlineEquipment, "")
                .eq(User::getId, userId);
        update(updateWrapper);
    }



    public JSONObject createUserToken(User user, String userIp) {
        JSONObject userinfo = new JSONObject();
        userinfo.put("userId", user.getId());
        userinfo.put("account", user.getAccount());
        userinfo.put("username", user.getName());
        userinfo.put("role", user.getRole());
        userinfo.put("sex", user.getSex());
        userinfo.put("portrait", user.getPortrait());
        userinfo.put("phone", user.getPhone());
        userinfo.put("email", user.getEmail());
        //生成用户token
        userinfo.put("token", JwtUtil.createToken(userinfo));
        ThreadUtil.execAsync(() -> {
            //记录登录操作
            userOperatedService.recordLogin(user.getId(), userIp);
            //更新同时在线人数
            updateRedisOnlineNum();
        });
        return userinfo;
    }

    public void updateRedisOnlineNum() {
        Integer onlineNum = webSocketService.getOnlineNum();
        String key = "onlineNum#" + DateUtil.today();
        Integer redisOnlineNum = (Integer) redisUtils.get(key);
        if (null == redisOnlineNum) {
            redisUtils.set(key, onlineNum, 25 * 60 * 60);
        }
        if (onlineNum > redisOnlineNum) {
            redisUtils.set(key, onlineNum, 25 * 60 * 60);
        }
    }


}
