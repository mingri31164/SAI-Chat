package com.mingri.service.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.user.req.UpdateUserSetReq;
import com.mingri.model.vo.user.entity.UserSet;

/**
 * 用户设置表 服务类
 */
public interface UserSetService extends IService<UserSet> {

    UserSet getUserSet(String userId);

    boolean updateUserSet(String userId, UpdateUserSetReq updateUserSetReq);
}
