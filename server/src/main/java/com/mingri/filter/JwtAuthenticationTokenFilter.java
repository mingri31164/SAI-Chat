package com.mingri.filter;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mingri.constant.JwtClaimsConstant;
import com.mingri.constant.MessageConstant;
import com.mingri.constant.RedisConstant;
import com.mingri.context.BaseContext;
import com.mingri.entity.LoginUser;
import com.mingri.exception.LoginFailedException;
import com.mingri.properties.JwtProperties;
import com.mingri.result.Result;
import com.mingri.utils.CacheUtil;
import com.mingri.utils.JwtUtil;
import com.mingri.utils.RedisUtils;
import com.mingri.utils.WebUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private CacheUtil cacheUtil;

    /**
     * @Description: 过滤器拦截请求
     * @Author: mingri31164
     * @Date: 2025/1/21 21:00
     **/
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取 token
        String token = request.getHeader(jwtProperties.getTokenName());

        // 判空
        if (!StringUtils.hasText(token)) {
            // 如果没有令牌，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        // 校验令牌
        Long userId;
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户的id：{}", userId);
            // 验证是否在其他地方登录
            String cacheToken = cacheUtil.getUserSessionCache(userId.toString());
            if (StrUtil.isBlank(cacheToken)){
                throw new LoginFailedException(MessageConstant.AUTHENTICATION_FAILED);
            }
            else if (!cacheToken.equals(token)){
                Result<Object> error = Result.error
                        (HttpStatus.FORBIDDEN.value(), MessageConstant.LOGIN_IN_OTHER_PLACE);
                String json = JSON.toJSONString(error);
                WebUtils.renderString(response,json);
                return;
            }
            BaseContext.setCurrentId(userId);
        } catch (Exception e) {
            // 解析失败
            throw new LoginFailedException(MessageConstant.TOKEN_ERROR);
        }

        // 从 Redis 中获取用户信息
        String redisKey = RedisConstant.USER_INFO_PREFIX + userId;
        LoginUser loginUser = (LoginUser) redisUtils.get(redisKey);

        // 判断获取到的用户信息是否为空
        if (Objects.isNull(loginUser)) {
            throw new LoginFailedException(MessageConstant.USER_NOT_LOGIN);
        }

        // 将用户信息放入 SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser,
                        null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 继续请求
        filterChain.doFilter(request, response);
    }
}
