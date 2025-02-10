package com.mingri.controller.hello;

import com.mingri.properties.JwtProperties;
import com.mingri.utils.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private CacheUtil cacheUtil;
    @RequestMapping("/hello")
    @PreAuthorize("hasAuthority('system:dept:list')") //单个权限
//    @PreAuthorize("hasAnyAuthority('user','admin')") //多个权限（只要有其中之一）
//    @PreAuthorize("hasRole('user')") //内部拼接(ROLE_user)比对
//    @PreAuthorize("@myEx.hasAuthority('system:dept:list')") //自定义权限校验
    public String hello(){
        return "欢迎，开始你新的学习旅程吧";
    }

}