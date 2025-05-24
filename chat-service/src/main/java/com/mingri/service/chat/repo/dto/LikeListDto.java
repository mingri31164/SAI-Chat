package com.mingri.service.chat.repo.dto;

import lombok.Data;

@Data
public class LikeListDto {
    private String id;
    private String userId;
    private String name;
    private String portrait;
    private String remark;
}
