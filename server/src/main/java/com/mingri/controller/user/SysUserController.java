package com.mingri.controller.user;


import com.mingri.annotation.UrlLimit;
import com.mingri.constant.JwtClaimsConstant;
import com.mingri.constant.type.LimitKeyType;
import com.mingri.dto.user.SysUpdateDTO;
import com.mingri.dto.user.SysUserLoginDTO;
import com.mingri.dto.user.SysUserRegisterDTO;
import com.mingri.entity.SysUser;
import com.mingri.properties.JwtProperties;
import com.mingri.result.Result;
import com.mingri.service.ISysUserService;
import com.mingri.utils.CacheUtil;
import com.mingri.utils.JwtUtil;
import com.mingri.vo.SysUserInfoVO;
import com.mingri.vo.SysUserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@Api(tags = "用户相关接口")
@RequestMapping("/api/v1/user")
public class SysUserController {

    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private CacheUtil cacheUtil;


    /**
     * 登录
     * @param userLoginDTO
     * @return
     */
    @UrlLimit(keyType = LimitKeyType.IP)
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<SysUserLoginVO> login(@RequestBody SysUserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        SysUser loginUser = iSysUserService.login(userLoginDTO);
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, loginUser.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getExpireTime(),
                claims);

        cacheUtil.putUserSessionCache(String.valueOf(loginUser.getId()), token);
        SysUserLoginVO userLoginVO = SysUserLoginVO.builder()
                .userId(loginUser.getId())
                .userName(loginUser.getUserName())
                .type(loginUser.getUserType())
                .email(loginUser.getEmail())
                .avatar(loginUser.getAvatar())
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
    @ApiOperation("用户注册")
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
    @ApiOperation("退出登录")
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
    @ApiOperation("编辑用户信息")
    public Result update(@RequestBody SysUpdateDTO sysUpdateDTO){
        log.info("编辑用户信息：{}", sysUpdateDTO);
        iSysUserService.updateUser(sysUpdateDTO);
        return Result.success();
    }


    @UrlLimit
    @ApiOperation("查询所有用户信息")
    @GetMapping("/list/map")
    public Object listMapUser() {
        Map<String, SysUserInfoVO> result = iSysUserService.listMapUser();
        return Result.success(result);
    }


    @UrlLimit
    @ApiOperation("查询所有在线用户")
    @GetMapping("/online/web")
    public Object onlineWeb() {
        List<String> result = iSysUserService.onlineWeb();
        return Result.success(result);
    }



}
