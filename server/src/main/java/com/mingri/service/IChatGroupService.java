package com.mingri.service;

import com.mingri.entity.ChatGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface IChatGroupService extends IService<ChatGroup> {

    List<ChatGroup> chatGroupList(String userId);
}
