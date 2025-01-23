package com.mingri.controller.hello;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 35238
 * @date 2023/7/10 0010 22:25
 */
@RestController
public class HelloController {

    @RequestMapping("/hello")
    @PreAuthorize("hasAuthority('system:dept:list')") //单个权限
//    @PreAuthorize("hasAnyAuthority('user','admin')") //多个权限（只要有其中之一）
//    @PreAuthorize("hasRole('user')") //内部拼接(ROLE_user)比对
//    @PreAuthorize("@myEx.hasAuthority('system:dept:list')") //自定义权限校验
    public String hello(){
        return "欢迎，开始你新的学习旅程吧";
    }

}