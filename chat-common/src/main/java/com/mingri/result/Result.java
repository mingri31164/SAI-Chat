package com.mingri.result;

import lombok.Data;
import java.io.Serializable;


@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 0;
        result.msg = "ok";
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 0;
        result.msg = "ok";
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = code;
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<T>();
        result.code = 0;
        result.msg = "fail";
        return result;
    }

}
