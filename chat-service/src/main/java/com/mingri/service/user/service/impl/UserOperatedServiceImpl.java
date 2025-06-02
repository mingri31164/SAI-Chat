package com.mingri.service.user.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.model.constant.UserOperatedType;
import com.mingri.service.user.repo.vo.dto.UserOperatedDto;
import com.mingri.service.user.repo.vo.req.LoginDetailsReq;
import com.mingri.service.user.repo.vo.entity.UserOperated;
import com.mingri.service.user.repo.mapper.UserOperatedMapper;
import com.mingri.service.user.service.UserOperatedService;
import com.mingri.toolkit.SnowflakeIdUtil;
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
        operated.setId(SnowflakeIdUtil.nextIdStr());
        operated.setUserId(id);
        operated.setContent(ip);
        operated.setType(UserOperatedType.Login);
        return save(operated);
    }

    @Override
    public List<UserOperatedDto> loginDetails(LoginDetailsReq loginDetailsReq) {
        List<UserOperatedDto> result = userOperatedMapper.loginDetails(loginDetailsReq.getIndex(), loginDetailsReq.getNum(), loginDetailsReq.getKeyword());
        return result;
    }

    @Override
    public Integer uniqueLoginNum(Date date) {
        Integer result = userOperatedMapper.uniqueLoginNum(date);
        return result;
    }

}
