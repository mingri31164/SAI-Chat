package com.mingri.test;

import cn.hutool.json.JSONObject;
import com.mingri.core.argument.Userid;
import com.mingri.core.sensitive.SensitiveService;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.talk.repo.vo.dto.CommentListDto;
import com.mingri.service.talk.repo.vo.req.TalkCommentListReq;
import com.mingri.service.talk.service.TalkCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;



@Slf4j
@RestController
@Tag(name = "功能测试接口")
@RequestMapping(path = "/test")
public class TestController {

    @Resource
    TalkCommentService  talkCommentService;
    @Resource
    SensitiveService sensitiveService;

    @RequestMapping(path = "/sensitive")
    @Operation(summary = "测试评论敏感词过滤")
    public JSONObject testSensitive(@Userid String userId, @RequestBody TalkCommentListReq talkCommentListReq){
//        String result = sensitiveService.replace("sb");
        List<CommentListDto> result = talkCommentService.talkCommentList(userId, talkCommentListReq);
        return ResultUtil.Succeed(result);
    }

}