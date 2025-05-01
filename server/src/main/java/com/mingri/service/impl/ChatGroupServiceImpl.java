package com.mingri.service.impl;

import com.mingri.entity.ChatGroup;
import com.mingri.mapper.ChatGroupMapper;
import com.mingri.service.IChatGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements IChatGroupService {


    @Override
    public List<ChatGroup> chatGroupList(String userId) {
        return null;
    }
}
