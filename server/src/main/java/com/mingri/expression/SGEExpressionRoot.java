package com.mingri.expression;

import com.mingri.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/23 0:30
 * @ClassName: SGEExpressionRoot
 * @Version: 1.0
 */

//@Component("myEx")
public class SGEExpressionRoot {

    /**
     * @Description: 自定义权限认证
     * @Author: mingri31164
     * @Date: 2025/1/23 0:49
     **/
    public boolean hasAuthority(String authority) {
        //获取当前用户权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> permissions = loginUser.getPermissions();

        //判断用户权限集合中是否存在authority
        return permissions.contains(authority);
    }

}
