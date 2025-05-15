package com.mingri.service.user.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mingri.model.constant.MailConstant;
import com.mingri.model.constant.MessageConstant;
import com.mingri.model.constant.RedisConstant;
import com.mingri.model.constant.type.BadgeType;
import com.mingri.model.constant.type.NotifyType;
import com.mingri.model.context.BaseContext;
import com.mingri.model.vo.chat.message.dto.NotifyDto;
import com.mingri.model.vo.sys.dto.SysUpdateDTO;
import com.mingri.model.vo.sys.dto.SysUserInfoDTO;
import com.mingri.model.vo.sys.dto.SysUserRegisterDTO;
import com.mingri.service.user.repo.entity.helper.LoginUser;
import com.mingri.service.user.repo.entity.SysUserDO;
import com.mingri.model.enumeration.UserStatus;
import com.mingri.model.enumeration.UserTypes;
import com.mingri.model.exception.BaseException;
import com.mingri.model.exception.EmailErrorException;
import com.mingri.model.exception.RegisterFailedException;
import com.mingri.service.user.repo.mapper.SysUserMapper;
import com.mingri.service.user.service.ISysUserService;
import com.mingri.service.websocket.service.WebSocketService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.core.utils.CacheUtil;
import com.mingri.core.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;


@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserDO> implements ISysUserService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private CacheUtil cacheUtil;



    public void register(SysUserRegisterDTO sysUserRegisterDTO) {

        // TODO 邮箱重复处理
//        if (lambdaQuery().eq(SysUser::getEmail, sysUserRegisterDTO.getEmail()).exists()) {
//            throw new RegisterFailedException(MessageConstant.EMAIL_EXIST);
//        }
        // 根据邮箱生成Redis键名
        String redisKey = MailConstant.CAPTCHA_CODE_KEY_PRE + sysUserRegisterDTO.getEmail();
        // 尝试从Redis获取现有的验证码
        Object oldCode = redisUtils.get(redisKey);
        if (oldCode == null){
            throw new EmailErrorException
                    (MessageConstant.PLEASE_GET_VERIFICATION_CODE_FIRST);
        }
        // 检查用户名是否已存在
        if (lambdaQuery().eq(SysUserDO::getUserName, sysUserRegisterDTO.getUserName()).exists()) {
            throw new RegisterFailedException(MessageConstant.ACCOUNT_EXIST);
        }

        if (sysUserRegisterDTO.getEmailCode() == null ||
                sysUserRegisterDTO.getEmailCode().isEmpty() ||
                !oldCode.equals(sysUserRegisterDTO.getEmailCode())){
            throw new EmailErrorException
                    (MessageConstant.EMAIL_VERIFICATION_CODE_ERROR);
        }
        SysUserDO sysUser = new SysUserDO();
        //对象属性拷贝
        BeanUtils.copyProperties(sysUserRegisterDTO, sysUser);
        sysUser.setId(IdUtil.simpleUUID());
        //passwordEncoder加密
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        //设置账号的状态，默认正常状态 0表示正常 1表示锁定
        sysUser.setStatus(UserStatus.NORMAL);
        sysUser.setBadge(Collections.singletonList("clover"));

        redisUtils.del(redisKey);

        log.info("注册时间：{}",sysUser.getUpdateTime());
        save(sysUser);
    }



    /**
     * @Description: 登录（security）
     * @Author: mingri31164
     * @Date: 2025/1/21 18:33
     **/
    @Override
    public void afterLogin(SysUserDO sysUser, LoginUser loginUser) {
        // 更新用户登录时间、等级荣誉
        sysUser.setLoginTime(new Date());
        updateUserBadge(sysUser.getId());
        updateById(sysUser);

        // 把完整的用户信息存入 Redis（userid 作为 key）
        redisUtils.set(
                RedisConstant.USER_INFO_PREFIX + sysUser.getId(),
                loginUser
        );
    }



    /**
     * @Description: 用户退出登录
     * @Author: mingri31164
     * @Date: 2025/1/21 21:36
     **/
    public void logout() {
        //获取我们在JwtAuthenticationTokenFilter类写的SecurityContextHolder对象中的用户id
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        //loginUser是我们在domain目录写好的实体类
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        //获取用户id
        String userid = loginUser.getSysUser().getId();

        //根据用户id，删除redis中的loginUser，注意我们的key是被 helper: 拼接过的，所以下面写完整key的时候要带上 longin:
        String key = RedisConstant.USER_INFO_PREFIX + userid.toString();
        //删除cache中的token值
        cacheUtil.clearUserSessionCache(userid.toString());
        redisUtils.del(key);
    }

    @Override
    //@DS("slave")
    public SysUserInfoDTO getUserById(String userId) {
        return baseMapper.getUserById(userId);
    }

    @Override
    //@DS("slave")
    public List<SysUserInfoDTO> listUser() {
        return baseMapper.listUser();
    }

    @Override
    public List<String> onlineWeb() {
        return webSocketService.getOnlineUser();
    }

    @Override
    //@DS("slave")
    public Map<String, SysUserInfoDTO> listMapUser() {
        return baseMapper.listMapUser();
    }

    @Override
    public void online(String userId) {
        NotifyDto notifyDto = new NotifyDto();
        notifyDto.setTime(new Date());
        notifyDto.setType(NotifyType.Web_Online);
        notifyDto.setContent(JSONUtil.toJsonStr(getUserById(userId)));
        webSocketService.sendNotifyToGroup(notifyDto);
    }

    @Override
    public void offline(String userId) {
        NotifyDto notifyDto = new NotifyDto();
        notifyDto.setTime(new Date());
        notifyDto.setType(NotifyType.Web_Offline);
        notifyDto.setContent(JSONUtil.toJsonStr(getUserById(userId)));
        //离线更新，已读列表（防止用户直接关闭浏览器等情况）
        log.info("离线更新，已读列表");
//        chatListService.read(cacheUtil.getUserReadCache(userId));
        webSocketService.sendNotifyToGroup(notifyDto);
    }

    @Override
    public void deleteExpiredUsers(LocalDate expirationDate) {
        LambdaQueryWrapper<SysUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(SysUserDO::getLoginTime, expirationDate);
        if (remove(queryWrapper)) {
            log.info("---清理过期用户成功---");
        }
    }

    @Override
    public void updateUserBadge(String id) {
        SysUserDO user = getById(id);
        if (user == null) return;
        List<String> badges = user.getBadge();
        if (badges == null) {
            badges = new ArrayList<>();
        }
        boolean isUpdate = false;
        // 是否是第一个用户
        if (count() == 1) {
            if (!badges.contains(BadgeType.Crown)) {
                badges.add(BadgeType.Crown);
                isUpdate = true;
            }
        }
        // 根据用户创建时间发放徽章
        long diffInDays = DateUtil.between(user.getCreateTime(), new Date(), DateUnit.DAY);
        if (diffInDays >= 0 && diffInDays <= 7) {
            if (!badges.contains(BadgeType.Clover)) {
                badges.add(BadgeType.Clover);
                isUpdate = true;
            }
        } else if (diffInDays > 7) {
            if (badges.contains(BadgeType.Clover)) {
                badges.remove(BadgeType.Clover);
                isUpdate = true;
            }
            if (!badges.contains(BadgeType.Diamond)) {
                badges.add(BadgeType.Diamond);
                isUpdate = true;
            }
        }
        if (isUpdate) {
            user.setBadge(badges);
            updateById(user);
        }
    }

    @Override
    public void initBotUser() {
        SysUserDO doubao = getById("doubao");
        if (doubao == null) {
            SysUserDO robot = new SysUserDO();
            robot.setId("mingri-doubao");
            robot.setUserName("豆包");
            robot.setEmail(IdUtil.simpleUUID() + "@robot.com");
            robot.setUserType(UserTypes.bot);
            save(robot);
        }
    }

    @Override
    public boolean updateUser(SysUpdateDTO sysUpdateDTO) {
        SysUserDO user = lambdaQuery().eq(SysUserDO::getUserName, sysUpdateDTO.getName()).one();
        if (user != null) {
            if (!user.getId().equals(BaseContext.getCurrentId()))
                throw new BaseException(MessageConstant.ACCOUNT_EXIST);
        } else {
            user = getById(BaseContext.getCurrentId());
        }
        user.setUserName(sysUpdateDTO.getName());
        user.setAvatar(sysUpdateDTO.getAvatar());
        return updateById(user);
    }


}
