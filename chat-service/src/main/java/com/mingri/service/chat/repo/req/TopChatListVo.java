package com.mingri.service.chat.repo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TopChatListVo {
    @NotNull(message = "会话id不能为空")
    private String chatListId;

    @NotNull(message = "是否置顶不能为空")
    private boolean isTop;

    public boolean isTop() {
        return isTop;
    }

    public void setIsTop(boolean top) {
        isTop = top;
    }
}
