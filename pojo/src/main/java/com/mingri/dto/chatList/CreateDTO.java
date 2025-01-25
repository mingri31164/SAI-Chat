package com.mingri.dto.chatList;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateDTO {
    @NotBlank(message = "目标不能为空~")
    private String targetId;
}
