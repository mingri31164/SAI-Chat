package com.mingri.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NotifyDto {
    private String type;
    private String content;
    private Date time;
    private String ext;
}
