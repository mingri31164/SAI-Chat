package com.mingri.service.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.entity.TalkPermission;
import com.mingri.service.chat.repo.mapper.TalkPermissionMapper;
import com.mingri.service.chat.service.TalkPermissionService;
import org.springframework.stereotype.Service;

/**
 * 说说查看权限 服务实现类
 */
@Service
public class TalkPermissionServiceImpl extends ServiceImpl<TalkPermissionMapper, TalkPermission> implements TalkPermissionService {

}
