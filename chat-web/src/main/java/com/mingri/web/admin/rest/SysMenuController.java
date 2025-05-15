package com.mingri.web.admin.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "权限相关接口")
@RequestMapping("/api/v1/sys-menu")
public class SysMenuController {

    /**
     * 自定义权限认证注解测试
     **/
//    @RequestMapping("/hello")
//    @PreAuthorize("hasAuthority('system:dept:list')") //单个权限
////    @PreAuthorize("hasAnyAuthority('user','admin')") //多个权限（只要有其中之一）
////    @PreAuthorize("hasRole('user')") //内部拼接(ROLE_user)比对
////    @PreAuthorize("@myEx.hasAuthority('system:dept:list')") //自定义权限校验
//    public String hello(){
//        return "欢迎，开始你新的学习旅程吧";
//    }

}
