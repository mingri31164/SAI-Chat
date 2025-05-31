package com.mingri.backend.rest;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.mingri.core.permission.UrlResource;
import com.mingri.core.argument.Userid;
import com.mingri.core.minio.MinioUtil;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.exception.BaseException;
import com.mingri.service.notify.repo.vo.dto.SystemNotifyDto;
import com.mingri.service.notify.repo.vo.req.DeleteNotifyReq;
import com.mingri.service.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;



@RestController("AdminNotifyController")
@Tag(name = "管理端-通知管理接口")
@RequestMapping("/admin/v1/api/notify")
public class NotifyController {

    @Resource
    NotifyService notifyService;

    @Resource
    MinioUtil minioUtil;

    /**
     * 系统通知列表
     */
    @GetMapping("/system/list")
    @Operation(summary = "获取系统通知列表")
    @UrlResource("admin")
    public JSONObject SystemListNotify(@Userid String userId) {
        List<SystemNotifyDto> result = notifyService.SystemListNotify(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 系统通知删除
     */
    @PostMapping("/system/delete")
    @Operation(summary = "删除系统通知")
    @UrlResource("admin")
    public JSONObject deleteNotify(@RequestBody DeleteNotifyReq deleteNotifyVo) {
        boolean result = notifyService.deleteNotify(deleteNotifyVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 系统通知创建
     */
    @PostMapping("/system/create")
    @Operation(summary = "创建系统通知")
    @UrlResource("admin")
    public JSONObject createNotify(@NotNull(message = "图片不能为空~") @RequestParam("file") MultipartFile file,
                                   @NotNull(message = "标题不能为空~") @RequestParam("title") String title,
                                   @NotNull(message = "内容不能为空~") @RequestParam("text") String text) {
        String url;
        try {
            url = minioUtil.upload(file.getInputStream(), "notify/" + IdUtil.randomUUID(), file.getContentType(), file.getSize());
        } catch (Exception e) {
            throw new BaseException("图片上传失败~");
        }
        boolean result = notifyService.createNotify(url, title, text);
        return ResultUtil.ResultByFlag(result);
    }
}
