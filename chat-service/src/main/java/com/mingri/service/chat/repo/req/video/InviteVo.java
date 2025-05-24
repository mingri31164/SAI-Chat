package com.mingri.service.chat.repo.req.video;

import lombok.Data;

@Data
public class InviteVo {
    private String userId;
    private boolean isOnlyAudio;

    public void setIsOnlyAudio(boolean isOnlyAudio) {
        this.isOnlyAudio = isOnlyAudio;
    }
}
