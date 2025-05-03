package com.mingri.controller.common;

import com.mingri.constant.MessageConstant;
import com.mingri.result.Result;
import com.mingri.service.CommonService;
import com.mingri.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/api/v1/common")
@Api(tags = "公共接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private CommonService commonService;


    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);

        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀   dfdfdf.png
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件名称
            String objectName = UUID.randomUUID().toString() + extension;

            //文件的请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }



    /**
     * @Description: 发送邮箱验证码
     * @Author: mingri31164
     * @Date: 2025/1/20 0:01
     **/
        @ApiOperation("发送邮箱验证码")
        @GetMapping("/get-code")
        public Result<String> sendEmailCaptcha(String email) {
            commonService.sendEmailCaptcha(email);
            return Result.success("发送成功");
        }

}
