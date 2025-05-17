package com.mingri.web.user.rest;

import cn.hutool.json.JSONObject;
import com.mingri.core.annotation.Userid;
import com.mingri.core.toolkit.ResultUtil;
import com.mingri.service.user.repo.req.UpdateUserSetVo;
import com.mingri.service.user.repo.entity.UserSet;
import com.mingri.service.user.service.UserSetService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/v1/api/user-set")
@RestController
public class UserSetController {

    @Resource
    UserSetService userSetService;

    @GetMapping("")
    public JSONObject getUserSet(@Userid String userId) {
        UserSet result = userSetService.getUserSet(userId);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/update")
    public JSONObject updateUserSet(@Userid String userId, @RequestBody UpdateUserSetVo updateUserSetVo) {
        boolean result = userSetService.updateUserSet(userId, updateUserSetVo);
        return ResultUtil.ResultByFlag(result);
    }

}
