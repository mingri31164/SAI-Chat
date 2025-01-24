package com.mingri.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mingri.annotation.UrlLimit;
import com.mingri.constant.JwtClaimsConstant;
import com.mingri.dto.SysUpdateDTO;
import com.mingri.dto.SysUserDTO;
import com.mingri.dto.SysUserLoginDTO;
import com.mingri.dto.SysUserRegisterDTO;
import com.mingri.entity.LoginUser;
import com.mingri.entity.PageQuery;
import com.mingri.entity.SysUser;
import com.mingri.enumeration.LimitKeyType;
import com.mingri.properties.JwtProperties;
import com.mingri.result.Result;
import com.mingri.service.ISysUserService;
import com.mingri.utils.CacheUtil;
import com.mingri.utils.JwtUtil;
import com.mingri.result.PageResult;
import com.mingri.vo.SysUserInfoVO;
import com.mingri.vo.SysUserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-22
 */
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

        LoginUser loginUser = iSysUserService.login(userLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, loginUser.getSysUser().getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getExpireTime(),
                claims);

        cacheUtil.putUserSessionCache(String.valueOf(loginUser.getSysUser().getId()), token);

        SysUserLoginVO userLoginVO = SysUserLoginVO.builder()
                .userId(loginUser.getSysUser().getId())
                .userName(loginUser.getUsername())
                .userType(loginUser.getSysUser().getUserType())
                .email(loginUser.getSysUser().getEmail())
                .avatar(loginUser.getSysUser().getAvatar())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }



    /**
     * @Description: 用户注册
     * @Author: mingri31164
     * @Date: 2025/1/20 18:13
     **/
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
    @PutMapping("/update")
    @ApiOperation("编辑用户信息")
    public Result update(@RequestBody SysUpdateDTO sysUpdateDTO){
        log.info("编辑用户信息：{}", sysUpdateDTO);
        iSysUserService.updateUser(sysUpdateDTO);
        return Result.success();
    }



//    @UrlLimit
    @GetMapping("/list")
    public Object listUser() {
        List<SysUserInfoVO> result = iSysUserService.listUser();
        return Result.success(result);
    }

//    @UrlLimit
    @GetMapping("/list/map")
    public Object listMapUser() {
        Map<String, SysUserInfoVO> result = iSysUserService.listMapUser();
        return Result.success(result);
    }

//    @UrlLimit
    @GetMapping("/online/web")
    public Object onlineWeb() {
        List<String> result = iSysUserService.onlineWeb();
        return Result.success(result);
    }

}
