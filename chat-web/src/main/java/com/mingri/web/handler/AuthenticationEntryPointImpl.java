package com.mingri.web.handler;

import com.alibaba.fastjson.JSON;
import com.mingri.model.constant.MessageConstant;
import com.mingri.model.result.Result;
import com.mingri.core.utils.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/22 23:00
 * @ClassName: AuthenticationEntryPointImpl
 * @Version: 1.0
 */

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {


    /**
     * @Description: 处理认证异常
     * @Author: mingri31164
     * @Date: 2025/1/22 23:01
     **/
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Result<Object> error = Result.error
                (HttpStatus.UNAUTHORIZED.value(), MessageConstant.AUTHENTICATION_FAILED);
        String json = JSON.toJSONString(error);
        WebUtils.renderString(response,json);
    }
}
