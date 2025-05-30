package com.mingri.service.chat.repo.vo.message.req;

import com.mingri.service.chat.repo.vo.message.dto.MsgContent;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SendMsgReq {

    //目标用户
    @NotNull(message = "目标用户不能为空")
    private String toUserId;

    //目标源:user,group
    private String source;

    //消息内容
    @NotNull(message = "消息内容不能为空")
    private MsgContent msgContent;

    //是否为转发消息
    private Boolean isForward;

    //转发消息的id
    private String fromMsgId;


}
