package com.mingri.service.user.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.constant.UserOperatedType;
import com.mingri.service.user.repo.dto.UserOperatedDto;
import com.mingri.service.user.repo.req.LoginDetailsVo;
import com.mingri.service.user.repo.entity.UserOperated;
import com.mingri.service.user.repo.mapper.UserOperatedMapper;
import com.mingri.service.user.service.UserOperatedService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserOperatedServiceImpl extends ServiceImpl<UserOperatedMapper, UserOperated> implements UserOperatedService {

    @Resource
    UserOperatedMapper userOperatedMapper;

    @Override
    public boolean recordLogin(String id, String ip) {
        UserOperated operated = new UserOperated();
        operated.setId(IdUtil.randomUUID());
        operated.setUserId(id);
        operated.setContent(ip);
        operated.setType(UserOperatedType.Login);
        return save(operated);
    }

    @Override
    public List<UserOperatedDto> loginDetails(LoginDetailsVo loginDetailsVo) {
        List<UserOperatedDto> result = userOperatedMapper.loginDetails(loginDetailsVo.getIndex(), loginDetailsVo.getNum(), loginDetailsVo.getKeyword());
        return result;
    }

    @Override
    public Integer uniqueLoginNum(Date date) {
        Integer result = userOperatedMapper.uniqueLoginNum(date);
        return result;
    }

}
