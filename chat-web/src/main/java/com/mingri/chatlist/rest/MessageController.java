package com.mingri.chatlist.rest;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mingri.core.argument.UserRole;
import com.mingri.core.argument.Userid;
import com.mingri.core.minio.MinioUtil;
import com.mingri.core.toolkit.RedisUtils;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.constant.MsgType;
import com.mingri.service.chat.repo.vo.message.entity.Message;
import com.mingri.service.chat.repo.vo.message.entity.MessageRetraction;
import com.mingri.service.chat.repo.vo.message.dto.MsgContent;
import com.mingri.service.chat.repo.vo.message.req.MessageRecordReq;
import com.mingri.service.chat.repo.vo.message.req.ReeditMsgReq;
import com.mingri.service.chat.repo.vo.message.req.RetractionMsgReq;
import com.mingri.service.chat.repo.vo.message.req.SendMsgReq;
import com.mingri.service.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;



@RestController
@Tag(name = "消息基础接口")
@RequestMapping("/v1/api/message")
public class MessageController {

    @Resource
    MessageService messageService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public JSONObject sendMessage(@Userid String userId, @UserRole String role, @RequestBody SendMsgReq sendMsgReq) {
        Message result = messageService.sendMessage(userId, role, sendMsgReq, MsgType.User);
        return ResultUtil.Succeed(result);
    }

    /**
     * 撤回消息
     */
    @PostMapping("/retraction")
    @Operation(summary = "撤回消息")
    public JSONObject retractionMsg(@Userid String userId, @RequestBody RetractionMsgReq retractionMsgReq) {
        Message result = messageService.retractionMsg(userId, retractionMsgReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 重新编辑
     */
    @PostMapping("/reedit")
    @Operation(summary = "重新编辑")
    public JSONObject reeditMsg(@Userid String userId, @RequestBody ReeditMsgReq reeditMsgReq) {
        MessageRetraction result = messageService.reeditMsg(userId, reeditMsgReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 聊天记录
     */
    @PostMapping("/record")
    @Operation(summary = "获取聊天记录")
    public JSONObject messageRecord(@Userid String userId, @RequestBody MessageRecordReq messageRecordReq) {
        List<Message> result = messageService.messageRecord(userId, messageRecordReq);
        return ResultUtil.Succeed(result);
    }

    /**
     * 聊天记录（降序）
     */
    @PostMapping("/record/desc")
    @Operation(summary = "获取聊天记录（降序）")
    public JSONObject messageRecordDesc(@Userid String userId, @RequestBody MessageRecordReq messageRecordReq) {
        List<Message> result = messageService.messageRecordDesc(userId, messageRecordReq);
        return ResultUtil.Succeed(result);
    }


    /**
     * 发送文件
     */
    @PostMapping("/send/file")
    @Operation(summary = "发送文件")
    public JSONObject sendFile(HttpServletRequest request,
                               @Userid String userId,
                               @RequestHeader("msgId") String msgId) throws IOException {
        String url = messageService.sendFileOrImg(userId, msgId, request.getInputStream());
        return ResultUtil.Succeed(url);
    }


    /**
     * 发送文件（表单）
     */
    @PostMapping("/send/file/form")
    @Operation(summary = "发送文件（表单）")
    public JSONObject sendFile(@RequestParam("file") MultipartFile file,
                               @Userid String userId,
                               @RequestParam("msgId") String msgId) throws IOException {
        String url = messageService.sendFileOrImg(userId, msgId, file.getInputStream());
        return ResultUtil.Succeed(url);
    }

    /**
     * 发送图片
     */
    @PostMapping(value = "/send/Img")
    @Operation(summary = "发送图片")
    public JSONObject sendImg(HttpServletRequest request,
                              @Userid String userId,
                              @RequestHeader("msgId") String msgId) throws IOException {
        String url = messageService.sendFileOrImg(userId, msgId, request.getInputStream());
        return ResultUtil.Succeed(url);
    }


    /**
     * 获取文件
     */
    @GetMapping("/get/file")
    @Operation(summary = "获取文件")
    public ResponseEntity<InputStreamResource> getFile(HttpServletResponse response,
                                                       @Userid String userId,
                                                       @RequestHeader("msgId") String msgId) {
        MsgContent msgContent = messageService.getFileMsgContent(userId, msgId);
        JSONObject fileInfo = JSONUtil.parseObj(msgContent.getContent());
        String fileName = fileInfo.get("fileName").toString();
        InputStream inputStream = minioUtil.getObject(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.get("name").toString() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    /**
     * 获取媒体
     */
    @GetMapping("/get/media")
    @Operation(summary = "获取媒体")
    public JSONObject getMedia(@Userid String userId, @RequestParam("msgId") String msgId) {
        MsgContent msgContent = messageService.getFileMsgContent(userId, msgId);
        JSONObject fileInfo = JSONUtil.parseObj(msgContent.getContent());
        String fileName = fileInfo.get("fileName").toString();
        String url = (String) redisUtils.get(fileName);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.previewFile(fileName);
            redisUtils.set(fileName, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }

    /**
     * 语音消息转文字
     */
    @GetMapping("/voice/to/text")
    @Operation(summary = "语音消息转文字")
    public JSONObject voiceToText(@Userid String userId, @RequestParam("msgId") String msgId) {
        Message result = messageService.voiceToText(userId, msgId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 语音消息转文字
     */
    @GetMapping("/voice/to/text/from")
    @Operation(summary = "语音消息转文字（区分单聊/群聊）")
    public JSONObject voiceToTextFrom(@Userid String userId, @RequestParam("msgId") String msgId,
                                      @RequestParam("isChatGroupMessage") Boolean isChatGroupMessage) {
        Message result = messageService.voiceToText(userId, msgId, isChatGroupMessage);
        return ResultUtil.Succeed(result);
    }
}
