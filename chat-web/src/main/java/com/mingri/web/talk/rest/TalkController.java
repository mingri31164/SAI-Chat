package com.mingri.web.talk.rest;


import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.mingri.core.argument.Userid;
import com.mingri.core.minio.MinioUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.talk.repo.vo.dto.TalkListDto;
import com.mingri.service.talk.repo.vo.entity.Talk;
import com.mingri.service.talk.repo.vo.req.CreateTalkReq;
import com.mingri.service.talk.repo.vo.req.DeleteTalkReq;
import com.mingri.service.talk.repo.vo.req.DetailsTalkReq;
import com.mingri.service.talk.repo.vo.req.TalkListReq;
import com.mingri.service.talk.service.TalkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 说说 前端控制器
 */
@RestController
@Tag(name = "说说基础接口")
@RequestMapping("/v1/api/talk")
public class TalkController {

    @Resource
    TalkService talkService;

    @Resource
    MinioUtil minioUtil;

    @PostMapping("/list")
    @Operation(summary = "获取说说列表")
    public JSONObject talkList(@Userid String userId, @RequestBody TalkListReq talkListReq) {
        List<TalkListDto> result = talkService.talkList(userId, talkListReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/details")
    @Operation(summary = "获取说说详情")
    public JSONObject detailsTalk(@Userid String userId, @RequestBody DetailsTalkReq detailsTalkReq) {
        TalkListDto result = talkService.detailsTalk(userId, detailsTalkReq);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/create")
    @Operation(summary = "创建说说")
    public JSONObject createTalk(@Userid String userId, @RequestBody CreateTalkReq createTalkReq) {
        Talk result = talkService.createTalk(userId, createTalkReq);
        return ResultUtil.Succeed(result);
    }


    @PostMapping("/upload/img")
    @Operation(
            summary = "上传说说图片（原始流）",
            description = """
        ## 适用场景
        - 移动端APP或其他需要自定义元数据的客户端  
        - 通过HTTP Headers传递文件信息  

        ## 请求示例
        ```
        POST /upload/img
        Headers:
          talkId: "123"
          name: "image.jpg"
          type: "image/jpeg"
          size: "204800"
        Body: [文件二进制流]
        ```
        """
    )
    public JSONObject uploadImgTalk(HttpServletRequest request,
                                    @Userid String userId,
                                    @RequestHeader("talkId") String talkId,
                                    @RequestHeader("name") String name,
                                    @RequestHeader("type") String type,
                                    @RequestHeader("size") long size) throws IOException {
        String imgName = IdUtil.randomUUID() + name.substring(name.lastIndexOf("."));
        String imgPath = userId + "/img/" + imgName;
        minioUtil.uploadFile(request.getInputStream(), imgPath, size);
        Talk talk = talkService.updateTalkImg(userId, talkId, imgName);
        return ResultUtil.Succeed(talk);
    }

    @PostMapping("/upload/img/form")
    @Operation(
            summary = "上传说说图片（表单上传）",
            description = """
        ## 适用场景
        - 网页前端使用 `<form>` 或 `FormData` 上传  
        - 符合标准 multipart/form-data 格式  

        ## 请求示例
        ```
        POST /upload/img/form
        Content-Type: multipart/form-data

        --boundary
        Content-Disposition: form-data; name="file"; filename="image.jpg"
        [文件二进制数据]
        --boundary
        Content-Disposition: form-data; name="talkId"
        123
        --boundary--
        ```
        """
        )
        public JSONObject uploadImgTalk(MultipartFile file,
                                    @Userid String userId,
                                    @RequestParam("talkId") String talkId,
                                    @RequestParam("name") String name,
                                    @RequestParam("size") long size) throws IOException {
        String imgName = IdUtil.randomUUID() + name.substring(name.lastIndexOf("."));
        String imgPath = userId + "/img/" + imgName;
        minioUtil.uploadFile(file.getInputStream(), imgPath, size);
        Talk talk = talkService.updateTalkImg(userId, talkId, imgName);
        return ResultUtil.Succeed(talk);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除说说")
    public JSONObject deleteTalk(@Userid String userId, @RequestBody DeleteTalkReq deleteTalkReq) {
        boolean result = talkService.deleteTalk(userId, deleteTalkReq);
        return ResultUtil.ResultByFlag(result);
    }
}

