package com.mingri.service.handler;


import com.mingri.model.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.mingri.core.toolkit.ResultUtil;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Object exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return ResultUtil.Fail(ex.getMessage());
    }

}
