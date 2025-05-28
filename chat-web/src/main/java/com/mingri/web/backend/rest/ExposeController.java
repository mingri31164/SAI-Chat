package com.mingri.web.backend.rest;

import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.vo.chat.message.req.expose.ThirdsendMsgReq;
import com.mingri.service.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Tag(name = "第三方发送消息接口")
@RequestMapping("/v1/api/expose")
public class ExposeController {

    @Resource
    MessageService messengerService;

    @UrlFree
    @PostMapping("/send")
    @Operation(summary = "第三方发送消息")
    public Object thirdPartySendMsg(@Userid String userid, @RequestBody ThirdsendMsgReq sendMsgReq) {
        boolean result = messengerService.thirdPartySendMsg(userid, sendMsgReq);
        return ResultUtil.ResultByFlag(result);
    }
}
