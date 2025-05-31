package com.mingri.middleware.dynamic.thread.pool.sdk.types;

import lombok.*;

import java.io.Serializable;

/**
 * @Author: mingri31164
 * @CreateTime: 2024-10-22 22:00
 * @Description:
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = -2474596551402989285L;

    private String code;
    private String info;
    private T data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum Code {
        SUCCESS("0000", "调用成功"),
        UN_ERROR("0001", "调用失败"),
        ILLEGAL_PARAMETER("0002", "非法参数"),
        ;

        private String code;
        private String info;

    }

}

