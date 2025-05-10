package com.mingri.handler;

import com.alibaba.fastjson.JSON;
import com.mingri.common.constant.MessageConstant;
import com.mingri.common.result.Result;
import com.mingri.core.utils.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/22 23:24
 * @ClassName: AccessDeniedHandlerImpl
 * @Version: 1.0
 */

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * @Description: 处理授权异常
     * @Author: mingri31164
     * @Date: 2025/1/22 23:24
     **/
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Result<Object> error = Result.error
                (HttpStatus.FORBIDDEN.value(), MessageConstant.PERMISSION_NOT_EXIST);
        String json = JSON.toJSONString(error);
        WebUtils.renderString(response,json);
    }
}
