package com.mingri.service.chat.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.service.chat.repo.entity.Message;
import com.mingri.service.chat.repo.entity.ext.MsgContent;
import com.mingri.service.chat.repo.req.SendMsgVo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 消息表 服务类
 */
public interface MessageService extends IService<Message> {

    Message sendMessage(String userId, String role, SendMsgVo sendMsgVo, String type);

}
