package com.mingri.controller.user;

import com.mingri.api.constant.JwtClaimsConstant;
import com.mingri.api.constant.MessageConstant;
import com.mingri.api.constant.type.LimitKeyType;
import com.mingri.core.limit.UrlLimit;
import com.mingri.pojo.dto.user.SysUpdateDTO;
import com.mingri.pojo.dto.user.SysUserLoginDTO;
import com.mingri.pojo.dto.user.SysUserRegisterDTO;
import com.mingri.pojo.entity.login.LoginUser;
import com.mingri.pojo.entity.sys.SysUser;
import com.mingri.api.enumeration.UserStatus;
import com.mingri.api.exception.LoginFailedException;
import com.mingri.api.properties.JwtProperties;
import com.mingri.api.result.Result;
import com.mingri.service.ISysUserService;
import com.mingri.core.utils.CacheUtil;
import com.mingri.core.utils.JwtUtil;
import com.mingri.pojo.vo.sys.SysUserInfoVO;
import com.mingri.pojo.vo.sys.SysUserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Slf4j
@Tag(name = "用户相关接口")
@RequestMapping("/api/v1/user")
public class SysUserController {

    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private CacheUtil cacheUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 登录
     * @param userLoginDTO
     * @return
     */
    @UrlLimit(keyType = LimitKeyType.IP)
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<SysUserLoginVO> login(@RequestBody SysUserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        // 1️⃣ 执行认证
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getUserName(), userLoginDTO.getPassword());

        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            throw new LoginFailedException(MessageConstant.LOGIN_ERROR);
        }

        // 2️⃣ 获取认证后的 LoginUser
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        SysUser sysUser = loginUser.getSysUser();

        // 检查用户状态
        if (sysUser.getStatus().equals(UserStatus.FREEZE)) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3️⃣ 调用 Service 做业务操作（更新登录时间、写入 Redis 缓存等）
        iSysUserService.afterLogin(sysUser, loginUser);

        // 4️⃣ 生成 JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, sysUser.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getExpireTime(),
                claims
        );

        cacheUtil.putUserSessionCache(String.valueOf(sysUser.getId()), token);

        // 5️⃣ 构建返回对象
        SysUserLoginVO userLoginVO = SysUserLoginVO.builder()
                .userId(sysUser.getId())
                .userName(sysUser.getUserName())
                .type(sysUser.getUserType())
                .email(sysUser.getEmail())
                .avatar(sysUser.getAvatar())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }



    /**
     * @Description: 用户注册
     * @Author: mingri31164
     * @Date: 2025/1/20 18:13
     **/
    @UrlLimit(keyType = LimitKeyType.IP)
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result register(@RequestBody SysUserRegisterDTO userRegisterDTO){
        log.info("新增用户：{}",userRegisterDTO);
        iSysUserService.register(userRegisterDTO);
        return Result.success();
    }


    /**
     * 退出
     * @return
     */
    @UrlLimit
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        iSysUserService.logout();
        return Result.success();
    }


    /**
     * 编辑用户信息
     * @param sysUpdateDTO
     * @return
     */
    @UrlLimit
    @PutMapping("/update")
    @Operation(summary = "编辑用户信息")
    public Result update(@RequestBody SysUpdateDTO sysUpdateDTO){
        log.info("编辑用户信息：{}", sysUpdateDTO);
        iSysUserService.updateUser(sysUpdateDTO);
        return Result.success();
    }


    @UrlLimit
    @Operation(summary = "查询所有用户信息")
    @GetMapping("/list/map")
    public Object listMapUser() {
        Map<String, SysUserInfoVO> result = iSysUserService.listMapUser();
        return Result.success(result);
    }


    @UrlLimit
    @Operation(summary = "查询所有在线用户")
    @GetMapping("/online/web")
    public Object onlineWeb() {
        List<String> result = iSysUserService.onlineWeb();
        return Result.success(result);
    }



}
