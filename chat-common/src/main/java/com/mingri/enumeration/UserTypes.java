package com.mingri.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserTypes {

    admin(0, "admin"),
    user(1, "user"),
    bot(2, "bot");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;

    UserTypes(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
