package com.mingri.service.user.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.core.config.MinioConfig;
import com.mingri.core.permission.SecurityUtil;
import com.mingri.core.toolkit.*;
import com.mingri.model.constant.NotifyType;
import com.mingri.model.constant.UserRole;
import com.mingri.model.constant.UserStatus;
import com.mingri.model.exception.BaseException;
import com.mingri.model.vo.admin.req.user.*;
import com.mingri.model.vo.user.req.*;
import com.mingri.model.vo.user.dto.UserDto;
import com.mingri.model.vo.user.dto.login.QrCodeResult;
import com.mingri.model.vo.user.req.login.LoginReq;
import com.mingri.model.vo.user.req.login.QrCodeLoginReq;
import com.mingri.service.chat.service.ChatListService;
import com.mingri.service.mail.EmailService;
import com.mingri.service.notify.service.NotifyService;
import com.mingri.service.mail.VerificationCodeService;
import com.mingri.model.vo.user.entity.User;
import com.mingri.service.user.repo.mapper.UserMapper;
import com.mingri.service.user.service.UserOperatedService;
import com.mingri.service.user.service.UserService;
import com.mingri.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

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
    @Resource
    MinioUtil minioUtil;





    // 客户端

    @Override
    public List<UserDto> searchUser(SearchUserReq searchUserReq) {
        List<UserDto> users = userMapper.findUserByInfo(searchUserReq.getUserInfo());
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
    public boolean updateUserInfo(String userId, UpdateReq updateReq) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getName, updateReq.getName())
                .set(User::getPortrait, updateReq.getPortrait())
                .set(User::getSex, updateReq.getSex())
                .set(User::getBirthday, updateReq.getBirthday())
                .set(User::getSignature, updateReq.getSignature())
                .eq(User::getId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean updateUserInfo(String userId, UpdatePasswordReq updateVo) {
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
    public boolean register(RegisterReq registerReq) {
        //验证码校验
        String code = (String) redisUtils.get(registerReq.getEmail());
        if (code == null || !code.equals(registerReq.getCode())) {
            throw new BaseException("验证码错误或者已失效~");
        }
        redisUtils.del(registerReq.getEmail());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, registerReq.getAccount());
        if (count(queryWrapper) > 0) {
            throw new BaseException("账号已存在~");
        }
        User user = new User();
        user.setId(IdUtil.randomUUID());
        user.setName(registerReq.getUsername());
        user.setAccount(registerReq.getAccount());
        String passwordHash = SecurityUtil.hashPassword(registerReq.getPassword());
        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
        user.setBirthday(new Date());
        user.setSex("男");
        user.setEmail(registerReq.getEmail());
        user.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-portrait.jpg");
        return save(user);
    }

    @Override
    public boolean forget(ForgetReq forgetReq) {
        User user = getUserByAccount(forgetReq.getAccount());
        if (null == user) throw new BaseException("用户不存在~");
        //验证码校验
        String code = (String) redisUtils.get(user.getEmail());
        if (code == null || !code.equals(forgetReq.getCode())) {
            throw new BaseException("验证码错误或者已失效~");
        }
        redisUtils.del(user.getEmail());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, forgetReq.getAccount())
                .eq(User::getEmail, user.getEmail());
//        User user = new User();
//        user.setId(IdUtil.randomUUID());
//        user.setAccount(forgetVo.getAccount());
        String passwordHash = SecurityUtil.hashPassword(forgetReq.getPassword());
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
    public JSONObject validateLogin(LoginReq loginReq, String userIp, boolean isAdmin) {
        // 获取用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(User::getAccount, loginReq.getAccount());
        User user = getOne(queryWrapper);
        if (null == user) {
            return ResultUtil.Fail("用户名或密码错误~");
        }
        if (!SecurityUtil.verifyPassword(loginReq.getPassword(), user.getPassword())) {
            return ResultUtil.Fail("用户名或密码错误~");
        }
        if (isAdmin && !UserRole.Admin.equals(user.getRole())) {
            return ResultUtil.Fail("您非管理员~");
        }
        JSONObject userinfo = createUserToken(user, userIp);
        user.setOnlineEquipment(loginReq.getOnlineEquipment());
        boolean isSave = updateById(user);
        return isSave?ResultUtil.Succeed(userinfo): ResultUtil.Fail("登录失败~");
    }

    @Override
    public JSONObject validateQrCodeLogin(QrCodeLoginReq qrCodeLoginReq, String userid) {
        // 获取用户
        User user = getById(userid);
        if (null == user) {
            return ResultUtil.Fail("用户不存在~");
        }
        String result = (String) redisUtils.get(qrCodeLoginReq.getKey());
        if (null == result) {
            return ResultUtil.Fail("二维码已失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        JSONObject userinfo = createUserToken(user, qrCodeResult.getIp());
        qrCodeResult.setStatus("success");
        qrCodeResult.setExtend(userinfo);
        redisUtils.set(qrCodeLoginReq.getKey(), JSONUtil.toJsonStr(qrCodeResult), 60);
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

    @Override
    public List<User> getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String createThirdPartyUser(MultipartFile portrait, String name) {
        String userId = IdUtil.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setAccount(IdUtil.objectId());
        String password = RandomUtil.randomString(8);
        String passwordHash = SecurityUtil.hashPassword(password);
        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
        user.setBirthday(new Date());
        user.setRole(UserRole.Third);
        user.setSex("男");
        String url;
        try {
            url = minioUtil.upload(portrait.getInputStream(), userId + "-portrait"
                    , portrait.getContentType(), portrait.getSize());
        } catch (Exception e) {
            throw new BaseException("头像上传失败~");
        }
        user.setPortrait(url);
        save(user);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateThirdPartyUser(MultipartFile portrait, String name, String userId) {
        String url;
        try {
            url = minioUtil.upload(portrait.getInputStream(), userId + "-portrait"
                    , portrait.getContentType(), portrait.getSize());
        } catch (Exception e) {
            throw new BaseException("头像上传失败~");
        }
        url += "?t=" + System.currentTimeMillis();
        User user = getById(userId);
        user.setPortrait(url);
        user.setName(name);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteThirdPartyUser(String userId) {
        return removeById(userId);
    }

    @Override
    public boolean allUserOffline() {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getIsOnline, false);
        return update(updateWrapper);
    }






    // 管理端

    @Resource
    EmailService emailService;

    @Override
    public Page<User> userList(UserListReq userListReq) {
        Page<User> page = new Page<>(userListReq.getCurrentPage(), userListReq.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getId, User::getAccount, User::getName, User::getPortrait,
                User::getSex, User::getBirthday, User::getSignature, User::getPhone,
                User::getEmail, User::getLastOptTime, User::getStatus, User::getIsOnline,
                User::getRole, User::getCreateTime, User::getUpdateTime);
        if (StringUtils.isNotBlank(userListReq.getKeyword())) {
            queryWrapper.and(query -> {
                query.like(User::getName, userListReq.getKeyword())
                        .or()
                        .like(User::getAccount, userListReq.getKeyword())
                        .or()
                        .like(User::getEmail, userListReq.getKeyword())
                        .or()
                        .like(User::getPhone, userListReq.getKeyword());
            });
        }
        if (StringUtils.isNotBlank(userListReq.getOnlineStatus())) {
            if (userListReq.getOnlineStatus().equals("online")) {
                queryWrapper.eq(User::getIsOnline, true);
            }
            if (userListReq.getOnlineStatus().equals("offline")) {
                queryWrapper.eq(User::getIsOnline, false);
            }
        }
        queryWrapper.orderByDesc(User::getCreateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public boolean createUser(CreateUserReq createUserReq) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, createUserReq.getAccount());
        if (count(queryWrapper) > 0) {
            throw new BaseException("账号已存在~");
        }
        User user = new User();
        user.setId(IdUtil.randomUUID());
        user.setName(createUserReq.getName());
        user.setAccount(createUserReq.getAccount());
        String password = RandomUtil.randomString(8);
        String passwordHash = SecurityUtil.hashPassword(password);
        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
        user.setBirthday(new Date());
        user.setSex("男");
        user.setEmail(createUserReq.getEmail());
        user.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-portrait.jpg");

        //密码发送邮件
        if (save(user)) {
            Context context = new Context();
            context.setVariable("username", createUserReq.getName());
            context.setVariable("account", createUserReq.getAccount());
            context.setVariable("password", password);
            emailService.sendHtmlMessage(createUserReq.getEmail(), "Mingri用户密码", "email_password_template.html", context);
        }

        return true;
    }

    @Override
    public boolean updateUser(UpdateUserReq updateUserReq) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getName, updateUserReq.getName())
                .set(User::getEmail, updateUserReq.getEmail())
                .set(User::getPhone, updateUserReq.getPhone())
                .eq(User::getId, updateUserReq.getId());
        return update(updateWrapper);
    }

    @Override
    public boolean disableUser(String userId, DisableUserReq disableUserReq) {
        if (userId.equals(disableUserReq.getUserId())) {
            throw new BaseException("不能禁用自己~");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getStatus, UserStatus.Disable)
                .eq(User::getId, disableUserReq.getUserId());
        return update(updateWrapper);
    }

    @Override
    public boolean undisableUser(UndisableUserReq undisableUserReq) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getStatus, UserStatus.Normal)
                .eq(User::getId, undisableUserReq.getUserId());
        return update(updateWrapper);
    }

    @Override
    public boolean deleteUser(String userId, DeleteUserReq deleteUserReq) {
        if (userId.equals(deleteUserReq.getUserId())) {
            throw new BaseException("不能删除自己~");
        }
        User user = getById(deleteUserReq.getUserId());
        if (UserRole.Third.equals(user.getRole())) {
            throw new BaseException("第三方用户不能删除，请到会话中删除~");
        }
        return removeById(deleteUserReq.getUserId());
    }

    @Override
    public boolean restPassword(ResetPasswordReq resetPasswordReq) {
        User user = getById(resetPasswordReq.getUserId());
        if (null == user) {
            throw new BaseException("用户不存在~");
        }
        String password = RandomUtil.randomString(8);
        String passwordHash = SecurityUtil.hashPassword(password);
        user.setPassword(passwordHash);
        //密码发送邮件
        if (updateById(user)) {
            Context context = new Context();
            context.setVariable("username", user.getName());
            context.setVariable("account", user.getAccount());
            context.setVariable("password", password);
            emailService.sendHtmlMessage(user.getEmail(), "Mingri用户密码", "email_password_template.html", context);
        }
        return true;
    }

    @Override
    public boolean setAdmin(String userId, SetAdminReq setAdminReq) {
        if (userId.equals(setAdminReq.getUserId())) {
            throw new BaseException("不能操作自己~");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getRole, UserRole.Admin)
                .eq(User::getId, setAdminReq.getUserId());
        return update(updateWrapper);
    }

    @Override
    public boolean cancelAdmin(String userId, CancelAdminReq cancelAdminReq) {
        if (userId.equals(cancelAdminReq.getUserId())) {
            throw new BaseException("不能操作自己~");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getRole, UserRole.User)
                .eq(User::getId, cancelAdminReq.getUserId());
        return update(updateWrapper);
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
