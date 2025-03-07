package com.mingri.controller.user;


import com.mingri.annotation.UrlLimit;
import com.mingri.dto.message.RecallDTO;
import com.mingri.dto.message.RecordDTO;
import com.mingri.dto.message.SendMessageDTO;
import com.mingri.entity.Message;
import com.mingri.result.Result;
import com.mingri.service.IMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;



@Api(tags = "消息相关接口")
@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    @Resource
    IMessageService messageService;

    @UrlLimit(maxRequests = 100)
    @ApiOperation("发送消息")
    @PostMapping("/send")
    public Object send(@RequestBody @Valid SendMessageDTO sendMessageDTO) {
        Message result = messageService.send(sendMessageDTO);
        return Result.success(result);
    }

    @UrlLimit
    @ApiOperation("查询聊天记录")
    @PostMapping("/record")
    public Object record(@RequestBody @Valid RecordDTO recordDTO) {
        List<Message> result = messageService.record(recordDTO);
        return Result.success(result);
    }

    @UrlLimit
    @ApiOperation("撤回消息")
    @PostMapping("/recall")
    public Object recall(@RequestBody @Valid RecallDTO recallDTO) {
        Message result = messageService.recall(recallDTO);
        return Result.success(result);
    }

}
