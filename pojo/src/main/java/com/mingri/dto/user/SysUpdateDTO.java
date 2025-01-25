package com.mingri.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/24 21:06
 * @ClassName: SysUpdateDTO
 * @Version: 1.0
 */

@Data
public class SysUpdateDTO {

    @NotBlank(message = "用户名不能为空~")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9]{2,15}$",
            message = "用户名只能包含英文字母和数字，且必须以英文字母开头，长度为[3-16]位~"
    )
    private String name;
    private String avatar;

}
