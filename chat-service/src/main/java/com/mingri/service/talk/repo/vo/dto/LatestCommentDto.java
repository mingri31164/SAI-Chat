package com.mingri.service.talk.repo.vo.dto;

import com.mingri.core.sensitive.SensitiveField;
import lombok.Data;

@Data
public class LatestCommentDto {
    private String name;
    private String remake;

    @SensitiveField
    private String content;
}
