package com.mingri.model.vo.chat.chatlist.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TopChatListReq {
    @NotNull(message = "会话id不能为空")
    private String chatListId;

    @NotNull(message = "是否置顶不能为空")
    private boolean isTop;

    public boolean isTop() {
        return isTop;
    }

}
