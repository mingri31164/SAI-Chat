package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mingri.core.permission.UrlFree;
import com.mingri.core.argument.UserIp;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.RedisUtils;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.exception.BaseException;
import com.mingri.service.user.repo.vo.dto.login.QrCodeResult;
import com.mingri.service.user.repo.vo.req.login.qr.ResultReq;
import com.mingri.service.user.repo.vo.req.login.qr.StatusReq;
import com.mingri.service.user.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.simpleframework.xml.Path;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Tag(name = "二维码基础接口")
@RequestMapping("/qr")
public class QrCodeController {

    @Resource
    RedisUtils redisUtils;

    @Resource
    QrCodeService qrCodeService;

    @GetMapping("/code")
    @Operation(summary = "创建二维码")
    @UrlFree
    public JSONObject code(@UserIp String userIp, @Userid String userId, @Path("action") String action) {
        String key = qrCodeService.createQrCode(action, userIp, userId);
        return ResultUtil.Succeed(key);
    }

    @PostMapping("/code/result")
    @Operation(summary = "获取二维码")
    @UrlFree
    public JSONObject result(@RequestBody ResultReq resultReq) {
        String result = (String) redisUtils.get(resultReq.getKey());
        if (null == result) {
            throw new BaseException("二维码失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        return ResultUtil.Succeed(qrCodeResult);
    }

    @GetMapping("/code/status")
    @Operation(summary = "获取二维码状态")
    public JSONObject status(@RequestBody StatusReq statusReq) {
        String result = (String) redisUtils.get(statusReq.getKey());
        if (null == result) {
            throw new BaseException("二维码失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        qrCodeResult.setStatus("scan");
        Long ttl = redisUtils.getExpire(statusReq.getKey());
        if (ttl > 0) {
            redisUtils.set(statusReq.getKey(), JSONUtil.toJsonStr(qrCodeResult), ttl);
        } else {
            redisUtils.set(statusReq.getKey(), JSONUtil.toJsonStr(qrCodeResult));
        }
        return ResultUtil.Succeed(qrCodeResult);
    }
}
