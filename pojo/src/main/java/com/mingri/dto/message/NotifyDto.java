package com.mingri.dto.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class NotifyDto {
    private String type;
    private String content;

    private Date time;
    private String ext;
}
