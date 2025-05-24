package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mingri.core.annotation.UrlFree;
import com.mingri.core.annotation.UserIp;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.RedisUtils;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.model.exception.BaseException;
import com.mingri.model.vo.user.dto.login.QrCodeResult;
import com.mingri.model.vo.user.req.login.qr.ResultReq;
import com.mingri.model.vo.user.req.login.qr.StatusReq;
import com.mingri.service.user.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Path;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/qr")
@Slf4j
public class QrCodeController {

    @Resource
    RedisUtils redisUtils;

    @Resource
    QrCodeService qrCodeService;

    @GetMapping("/code")
    @UrlFree
    public JSONObject code(@UserIp String userIp, @Userid String userId, @Path("action") String action) {
        String key = qrCodeService.createQrCode(action, userIp, userId);
        return ResultUtil.Succeed(key);
    }

    @PostMapping("/code/result")
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
