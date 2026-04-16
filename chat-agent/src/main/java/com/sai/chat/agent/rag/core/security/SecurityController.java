/*
 * 安全控制器
 */

package com.sai.chat.agent.rag.core.security;

import com.sai.chat.agent.framework.convention.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 安全控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final PermissionValidator permissionValidator;
    private final ContentSafetyFilter contentSafetyFilter;

    /**
     * 验证权限
     */
    @PostMapping("/permission/validate")
    public Result<PermissionValidator.PermissionResult> validatePermission(
            @RequestBody PermissionValidator.PermissionContext context) {
        
        PermissionValidator.PermissionResult result = permissionValidator.validate(context);
        
        return new Result<PermissionValidator.PermissionResult>()
                .setCode(Result.SUCCESS_CODE)
                .setData(result);
    }

    /**
     * 批量验证权限
     */
    @PostMapping("/permission/batch-validate")
    public Result<Map<String, PermissionValidator.PermissionResult>> batchValidatePermission(
            @RequestBody List<PermissionValidator.PermissionContext> contexts) {
        
        Map<String, PermissionValidator.PermissionResult> results = 
                permissionValidator.batchValidate(contexts);
        
        return new Result<Map<String, PermissionValidator.PermissionResult>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(results);
    }

    /**
     * 检查用户权限
     */
    @GetMapping("/permission/check")
    public Result<Boolean> hasPermission(
            @RequestParam String userId,
            @RequestParam String permission) {
        
        boolean hasPermission = permissionValidator.hasPermission(userId, permission);
        
        return new Result<Boolean>()
                .setCode(Result.SUCCESS_CODE)
                .setData(hasPermission);
    }

    /**
     * 获取用户权限列表
     */
    @GetMapping("/permission/user/{userId}")
    public Result<java.util.Set<String>> getUserPermissions(@PathVariable String userId) {
        java.util.Set<String> permissions = permissionValidator.getUserPermissions(userId);
        
        return new Result<java.util.Set<String>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(permissions);
    }

    /**
     * 过滤输入内容
     */
    @PostMapping("/content/filter-input")
    public Result<ContentSafetyFilter.SafetyResult> filterInput(
            @RequestParam String content,
            @RequestParam String userId) {
        
        ContentSafetyFilter.SafetyResult result = contentSafetyFilter.filterInput(content, userId);
        
        return new Result<ContentSafetyFilter.SafetyResult>()
                .setCode(Result.SUCCESS_CODE)
                .setData(result);
    }

    /**
     * 过滤输出内容
     */
    @PostMapping("/content/filter-output")
    public Result<ContentSafetyFilter.SafetyResult> filterOutput(
            @RequestParam String content,
            @RequestParam String userId) {
        
        ContentSafetyFilter.SafetyResult result = contentSafetyFilter.filterOutput(content, userId);
        
        return new Result<ContentSafetyFilter.SafetyResult>()
                .setCode(Result.SUCCESS_CODE)
                .setData(result);
    }

    /**
     * 获取内容安全规则
     */
    @GetMapping("/content/rules")
    public Result<List<ContentSafetyFilter.ContentRule>> getContentRules() {
        List<ContentSafetyFilter.ContentRule> rules = contentSafetyFilter.getAllRules();
        
        return new Result<List<ContentSafetyFilter.ContentRule>>()
                .setCode(Result.SUCCESS_CODE)
                .setData(rules);
    }

    /**
     * 获取用户违规次数
     */
    @GetMapping("/violation/count")
    public Result<Integer> getViolationCount(@RequestParam String userId) {
        int count = contentSafetyFilter.getUserViolationCount(userId);
        
        return new Result<Integer>()
                .setCode(Result.SUCCESS_CODE)
                .setData(count);
    }
}