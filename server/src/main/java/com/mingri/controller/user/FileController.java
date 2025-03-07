package com.mingri.controller.user;

import com.mingri.annotation.UrlLimit;
import com.mingri.dto.file.*;
import com.mingri.result.Result;
import com.mingri.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/28 22:28
 * @ClassName: FileController
 * @Version: 1.0
 */

@Api(tags = "文件传输接口")
@RestController
@Slf4j
@RequestMapping("/api/v1/file")
public class FileController {

    @Resource
    FileService fileService;

    /**
     * 发送offer
     */
    @UrlLimit
    @ApiOperation("发送WebRTC中的offer，发起文件传输")
    @PostMapping("/offer")
    public Object offer(String userId, @RequestBody OfferDTO offerDTO) {
        boolean result = fileService.offer(userId, offerDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 发送answer
     */
    @UrlLimit
    @ApiOperation("发送WebRTC中的answer，响应offer")
    @PostMapping("/answer")
    public Object answer(String userId, @RequestBody AnswerDTO answerDTO) {
        boolean result = fileService.answer(userId, answerDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 发送candidate
     */
    @UrlLimit
    @ApiOperation("发送WebRTC中的candidate，用于建立网络连接的候选地址信息")
    @PostMapping("/candidate")
    public Object candidate(String userId, @RequestBody CandidateDTO candidateDTO) {
        boolean result = fileService.candidate(userId, candidateDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 取消
     */
    @UrlLimit
    @ApiOperation("取消文件传输")
    @PostMapping("/cancel")
    public Object hangup(String userId, @RequestBody CancelDTO cancelDTO) {
        boolean result = fileService.cancel(userId, cancelDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 邀请
     */
    @UrlLimit
    @ApiOperation("邀请文件传输")
    @PostMapping("/invite")
    public Object invite(@RequestBody InviteDTO inviteDTO) {
        boolean result = fileService.invite(inviteDTO);
        return result? Result.success():Result.error();
    }

    /**
     * 同意
     */
    @UrlLimit
    @ApiOperation("接收文件传输")
    @PostMapping("/accept")
    public Object accept(String userId, @RequestBody AcceptDTO acceptDTO) {
        boolean result = fileService.accept(userId, acceptDTO);
        return result? Result.success():Result.error();
    }

}
