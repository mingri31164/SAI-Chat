package com.mingri.enumeration;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


@Getter
public enum NotifyTypeEnum {
    GROUP_CHAT(1, "groupChat"),
    REGISTER(2, "register"),
    LOGIN(3, "login"),
    ;


    private int type;
    private String msg;

    private static Map<Integer, NotifyTypeEnum> mapper;

    static {
        mapper = new HashMap<>();
        for (NotifyTypeEnum type : values()) {
            mapper.put(type.type, type);
        }
    }

    NotifyTypeEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    // 使用map优化查询
    public static NotifyTypeEnum typeOf(int type) {
        return mapper.get(type);
    }

    public static NotifyTypeEnum typeOf(String type) {
        return valueOf(type.toUpperCase().trim());
    }
}
