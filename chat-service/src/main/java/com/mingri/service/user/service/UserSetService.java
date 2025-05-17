package com.mingri.service.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.model.vo.user.req.UpdateUserSetVo;
import com.mingri.service.user.repo.entity.UserSet;

/**
 * 用户设置表 服务类
 */
public interface UserSetService extends IService<UserSet> {

    UserSet getUserSet(String userId);

    boolean updateUserSet(String userId, UpdateUserSetVo updateUserSetVo);
}
