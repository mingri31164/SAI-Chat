package com.mingri.user.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.argument.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.user.repo.vo.req.UpdateUserSetReq;
import com.mingri.service.user.repo.vo.entity.UserSet;
import com.mingri.service.user.service.UserSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@Tag(name = "用户设置接口")
@RequestMapping("/v1/api/user-set")
public class UserSetController {

    @Resource
    UserSetService userSetService;

    @GetMapping("")
    @Operation(summary = "获取用户设置")
    public JSONObject getUserSet(@Userid String userId) {
        UserSet result = userSetService.getUserSet(userId);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/update")
    @Operation(summary = "修改用户设置")
    public JSONObject updateUserSet(@Userid String userId, @RequestBody UpdateUserSetReq updateUserSetReq) {
        boolean result = userSetService.updateUserSet(userId, updateUserSetReq);
        return ResultUtil.ResultByFlag(result);
    }

}
