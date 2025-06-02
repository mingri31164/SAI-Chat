package com.mingri.core.permission;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class UrlPermitUtil {
    // 免验证Url
    private List<String> urls = new ArrayList<>();
    // 需要验证角色的url资源
    private Map<String, List<String>> roleUrl = new HashMap<>();

    {
        urls.add("/ws/**");

        // Swagger 相关接口
        urls.add("/v3/api-docs/**");
        urls.add("/swagger-ui/**");
        urls.add("/swagger-ui.html");
        urls.add("/doc.html");
        urls.add("/webjars/**");
        urls.add("/test/**");

        // 动态线程池组件 相关接口
        urls.add("/api/v1/dynamic/thread/pool/**");
    }

    public boolean verifyUrl(String permitUrl, List<String> urlArr) {
        for (String url : urlArr) {
            for (int index = 0; index < url.length(); index++) {
                if (url.charAt(index) == '*') {
                    return true;
                }
                if (permitUrl.length() == index + 1 && url.length() == index + 1) {
                    return true;
                }
                if (index == permitUrl.length() || permitUrl.charAt(index) != url.charAt(index)) {
                    break;
                }
            }
        }
        return false;
    }

    public boolean isPermitUrl(String url) {
        return verifyUrl(url, urls);
    }


    public List<String> getPermitAllUrl() {
        return urls;
    }

    public void addUrls(List<String> urls) {
        this.urls.addAll(urls);
    }

    public void addRoleUrl(String role, String url) {
        List<String> roles = roleUrl.get(url);
        if (roles == null) {
            roles = new ArrayList<>();
            roleUrl.put(url, roles);
        }
        roles.add(role);
    }

    public boolean isRoleUrl(String role, String url) {
        List<String> roles = roleUrl.get(url);
        if (roles == null) return true;
        for (String r : roles) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
