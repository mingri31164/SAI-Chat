package com.mingri.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/22 19:00
 * @ClassName: SysInfoVO
 * @Version: 1.0
 */

@Data
public class SysInfoVO implements Serializable {


    private String nickName;

    private String userName;

    private String avatar;

    private String phone;

    private String email;

    private Integer sex;

}
