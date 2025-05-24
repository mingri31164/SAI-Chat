package com.mingri.service.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.user.dto.UserOperatedDto;
import com.mingri.model.vo.user.req.LoginDetailsReq;
import com.mingri.model.vo.user.entity.UserOperated;

import java.util.Date;
import java.util.List;

public interface UserOperatedService extends IService<UserOperated> {
    boolean recordLogin(String id, String ip);

    List<UserOperatedDto> loginDetails(LoginDetailsReq loginDetailsReq);

    Integer uniqueLoginNum(Date date);
}
