package com.mingri.service.talk.repo.vo.dto;

import com.mingri.core.sensitive.SensitiveField;
import lombok.Data;

@Data
public class CommentListDto {
    private String id;
    private String userId;
    private String name;
    private String portrait;
    private String remark;
    @SensitiveField(bind = "content")
    private String content;
    private String createTime;
}
