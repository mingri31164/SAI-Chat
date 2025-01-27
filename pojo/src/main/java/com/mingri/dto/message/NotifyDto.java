package com.mingri.dto.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class NotifyDto {
    private String type;
    private String content;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date time;
    private String ext;
}
