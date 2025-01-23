package com.mingri.exception;

/**
 * 登录失败
 */
public class RegisterFailedException extends BaseException{
    public RegisterFailedException(String msg){
        super(msg);
    }
}
