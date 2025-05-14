package com.mingri.web.chat.chatgroup.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.util.ArrayList;

@Data
public class CreateChatGroupVo {

    @NotNull(message = "群名称不能为空~")
    public String name;
    public String notice; //公告
    public ArrayList<User> users; // 成员ids

    @Data
    public static class User {
        private String userId;
        private String name;
    }

}
