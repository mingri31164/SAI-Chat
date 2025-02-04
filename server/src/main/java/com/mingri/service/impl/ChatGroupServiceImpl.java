package com.mingri.service.impl;

import com.mingri.entity.ChatGroup;
import com.mingri.mapper.ChatGroupMapper;
import com.mingri.service.IChatGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements IChatGroupService {

}
