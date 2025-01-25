package com.mingri.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserTypes {

    ADMIN(0, "admin"),
    USER(1, "user"),
    BOT(2, "bot");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;

    UserTypes(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
