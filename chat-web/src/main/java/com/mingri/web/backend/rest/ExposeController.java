package com.mingri.web.backend.rest;

import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.chat.message.req.expose.ThirdsendMsgReq;
import com.mingri.service.chat.service.MessageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/api/expose")
public class ExposeController {

    @Resource
    MessageService messengerService;

    @UrlFree
    @PostMapping("/send")
    public Object thirdPartySendMsg(@Userid String userid, @RequestBody ThirdsendMsgReq sendMsgReq) {
        boolean result = messengerService.thirdPartySendMsg(userid, sendMsgReq);
        return ResultUtil.ResultByFlag(result);
    }
}
