package com.mingri.interceptor;


import com.mingri.constant.JwtClaimsConstant;
import com.mingri.context.BaseContext;
import com.mingri.properties.JwtProperties;
import com.mingri.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;


/**
 * jwt令牌校验的拦截器
 */
//@Component
//@Slf4j
//public class JwtTokenInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private JwtProperties jwtProperties;
//
//    /**
//     * 校验jwt
//     *
//     * @param request
//     * @param response
//     * @param handler
//     * @return
//     * @throws Exception
//     */
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //判断当前拦截到的是Controller的方法还是其他资源
//        if (!(handler instanceof HandlerMethod)) {
//            //当前拦截到的不是动态方法，直接放行
//            return true;
//        }
//
//        //1、从请求头中获取令牌
//        String token = request.getHeader(jwtProperties.getUserTokenName());
//
//        //2、校验令牌
//        try {
//            log.info("jwt校验:{}", token);
//            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
//            Object claims_userid = claims.get(JwtClaimsConstant.USER_ID);
//            log.info("jwt校验通过: {}",claims_userid);
//            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
//            log.info("当前用户的id：{}", userId);
//            BaseContext.setCurrentId(userId);
//            //3、通过，放行
//            return true;
//        } catch (Exception ex) {
//            //4、不通过，响应401状态码
//            response.setStatus(401);
//            return false;
//        }
//    }
//
//}
