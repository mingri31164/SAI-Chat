package com.mingri.core.config;

import com.mingri.core.annotation.UserInfo;
import com.mingri.core.annotation.UserIp;
import com.mingri.core.annotation.UserRole;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;


public class UserInfoArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserInfo.class) ||
                parameter.hasParameterAnnotation(Userid.class) ||
                parameter.hasParameterAnnotation(UserRole.class) ||
                parameter.hasParameterAnnotation(UserIp.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        if (parameter.hasParameterAnnotation(UserInfo.class)) {
            return request.getAttribute("userinfo");
        }
        if (parameter.hasParameterAnnotation(Userid.class)) {
            Map<String, Object> userinfo = (Map<String, Object>) request.getAttribute("userinfo");
            if (userinfo != null) {
                return userinfo.get("userId");
            }
        }
        if (parameter.hasParameterAnnotation(UserRole.class)) {
            Map<String, Object> userinfo = (Map<String, Object>) request.getAttribute("userinfo");
            if (userinfo != null) {
                return userinfo.get("role");
            }
        }
        if (parameter.hasParameterAnnotation(UserIp.class)) {
            String ipAddr = IpUtil.getIpAddr(request);
            if (ipAddr != null) {
                return ipAddr;
            }
        }
        return null;
    }
}
