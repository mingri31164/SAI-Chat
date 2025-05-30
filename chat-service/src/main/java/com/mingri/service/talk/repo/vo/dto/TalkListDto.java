package com.mingri.service.talk.repo.vo.dto;

import com.mingri.core.sensitive.SensitiveField;
import lombok.Data;

import java.util.List;


@Data
public class TalkListDto {
    private String userId;
    private String name;
    private String portrait;
    private String remark;
    private String talkId;

    @SensitiveField
    private TalkContentDto content;

    private List<LatestCommentDto> latestComment;
    private String time;
    private int likeNum;
    private int commentNum;
}
