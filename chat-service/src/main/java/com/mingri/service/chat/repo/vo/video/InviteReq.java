package com.mingri.service.chat.repo.vo.video;

import lombok.Data;

@Data
public class InviteReq {
    private String userId;
    private boolean isOnlyAudio;

    public void setIsOnlyAudio(boolean isOnlyAudio) {
        this.isOnlyAudio = isOnlyAudio;
    }
}
