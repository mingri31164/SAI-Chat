package com.mingri.dto.file;

import lombok.Data;

@Data
public class InviteDTO {
    private String userId;
    private FileInfo fileInfo;

    @Data
    public static class FileInfo {
        private String name;
        private long size;
    }
}
