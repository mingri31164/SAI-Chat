package com.mingri.service.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.user.repo.dto.UserOperatedDto;
import com.mingri.service.user.repo.req.LoginDetailsVo;
import com.mingri.service.user.repo.entity.UserOperated;

import java.util.Date;
import java.util.List;

public interface UserOperatedService extends IService<UserOperated> {
    boolean recordLogin(String id, String ip);

    List<UserOperatedDto> loginDetails(LoginDetailsVo loginDetailsVo);

    Integer uniqueLoginNum(Date date);
}
