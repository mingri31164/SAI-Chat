package com.mingri.service.filter;

import com.mingri.core.toolkit.JwtUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.core.permission.UrlPermitUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private final String TokenName = "x-token";

    @Resource
    private UrlPermitUtil urlPermitUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
            return;
        }

        String token = httpServletRequest.getHeader(TokenName);
        String url = httpServletRequest.getRequestURI();
        // 验证url是否需要验证
        if (!urlPermitUtil.isPermitUrl(url)) {
            try {
                Claims claims = JwtUtil.parseToken(token);
                setUserInfo(claims, url, httpServletRequest, httpServletResponse);
            } catch (Exception e) {
                tokenInvalid(httpServletResponse);
                return;
            }
        } else {
            if (StringUtils.isNotBlank(token)) {
                try {
                    Claims claims = JwtUtil.parseToken(token);
                    setUserInfo(claims, url, httpServletRequest, httpServletResponse);
                } catch (Exception e) {
                }
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    public void tokenInvalid(HttpServletResponse httpServletResponse) {
        try {
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            PrintWriter out = httpServletResponse.getWriter();
            out.write(ResultUtil.Forbidden().toJSONString(0));
            out.flush();
            out.close();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }

    public void setUserInfo(Claims claims, String url,
                            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // 设置用户信息
        Map<String, Object> map = new HashMap<>();
        claims.entrySet().stream().forEach(e -> map.put(e.getKey(), e.getValue()));
        //验证角色是否有权限
        String role = (String) map.get("role");
        if (!urlPermitUtil.isRoleUrl(role, url)) {
            tokenInvalid(httpServletResponse);
            return;
        }
        httpServletRequest.setAttribute("userinfo", map);
    }
}
