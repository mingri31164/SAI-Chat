package com.mingri.model.vo.sys.dto;

import com.baomidou.mybatisplus.annotation.TableField;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改用户对象")
public class SysUpdateDTO {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空~")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9]{2,15}$",
            message = "用户名只能包含英文字母和数字，且必须以英文字母开头，长度为[3-16]位~"
    )
    @TableField("user_name")
    private String name;

    @Schema(description = "头像")
    private String avatar;

}
