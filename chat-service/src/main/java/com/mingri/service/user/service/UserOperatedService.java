package com.mingri.service.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.user.repo.vo.dto.UserOperatedDto;
import com.mingri.service.user.repo.vo.req.LoginDetailsReq;
import com.mingri.service.user.repo.vo.entity.UserOperated;

import java.util.Date;
import java.util.List;

public interface UserOperatedService extends IService<UserOperated> {
    boolean recordLogin(String id, String ip);

    List<UserOperatedDto> loginDetails(LoginDetailsReq loginDetailsReq);

    Integer uniqueLoginNum(Date date);
}
