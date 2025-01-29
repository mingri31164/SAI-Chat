package com.mingri.dto.call;

import lombok.Data;

@Data
public class InviteDTO {
    private String userId;
    private boolean isOnlyAudio;

    public void setIsOnlyAudio(boolean isOnlyAudio) {
        this.isOnlyAudio = isOnlyAudio;
    }
}
