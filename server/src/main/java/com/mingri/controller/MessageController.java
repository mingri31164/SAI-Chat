package com.mingri.controller;


import com.mingri.annotation.UrlLimit;
import com.mingri.dto.message.RecallDTO;
import com.mingri.dto.message.RecordDTO;
import com.mingri.dto.message.SendMessageDTO;
import com.mingri.entity.Message;
import com.mingri.result.Result;
import com.mingri.service.IMessageService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;



@Api(tags = "消息操作相关接口")
@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    @Resource
    IMessageService messageService;

    @UrlLimit(maxRequests = 100)
    @PostMapping("/send")
    public Object send(@RequestBody @Valid SendMessageDTO sendMessageDTO) {
        Message result = messageService.send(sendMessageDTO);
        return Result.success(result);
    }

    @UrlLimit
    @PostMapping("/record")
    public Object record(@RequestBody @Valid RecordDTO recordDTO) {
        List<Message> result = messageService.record(recordDTO);
        return Result.success(result);
    }

    @UrlLimit
    @PostMapping("/recall")
    public Object recall(@RequestBody @Valid RecallDTO recallDTO) {
        Message result = messageService.recall(recallDTO);
        return Result.success(result);
    }

}
