/*
 * 权限验证器
 */

package com.sai.chat.agent.rag.core.security;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 权限验证器
 */
@Slf4j
@Component
public class PermissionValidator {

    private final Map<String, PermissionPolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> rolePermissions = new ConcurrentHashMap<>();

    public PermissionValidator() {
        initDefaultPolicies();
    }

    /**
     * 初始化默认权限策略
     */
    private void initDefaultPolicies() {
        // 管理员权限
        rolePermissions.put("ADMIN", Set.of(
                "agent:create", "agent:delete", "agent:update",
                "tool:register", "tool:unregister",
                "memory:read", "memory:write", "memory:delete",
                "config:read", "config:write",
                "observe:metrics", "observe:trace",
                "safety:bypass"
        ));

        // 普通用户权限
        rolePermissions.put("USER", Set.of(
                "agent:invoke",
                "memory:read", "memory:write",
                "observe:metrics:read"
        ));

        // 只读权限
        rolePermissions.put("VIEWER", Set.of(
                "memory:read",
                "observe:metrics:read"
        ));

        // Guest权限
        rolePermissions.put("GUEST", Set.of(
                "agent:invoke:limited"
        ));
    }

    /**
     * 验证权限
     */
    public PermissionResult validate(PermissionContext context) {
        String userId = context.getUserId();
        String permission = context.getPermission();
        String resource = context.getResourceId();

        // 获取用户角色
        Set<String> userRoles = getUserRoles(userId);
        
        if (userRoles.isEmpty()) {
            return PermissionResult.builder()
                    .allowed(false)
                    .reason("用户没有任何角色")
                    .userId(userId)
                    .permission(permission)
                    .build();
        }

        // 检查直接权限
        for (String role : userRoles) {
            Set<String> permissions = rolePermissions.get(role);
            if (permissions != null && permissions.contains(permission)) {
                // 检查资源级别权限
                if (!validateResourceLevel(context, role)) {
                    continue;
                }
                
                return PermissionResult.builder()
                        .allowed(true)
                        .userId(userId)
                        .roles(userRoles)
                        .permission(permission)
                        .resourceId(resource)
                        .build();
            }
        }

        // 检查策略权限
        PermissionPolicy policy = findApplicablePolicy(context);
        if (policy != null) {
            if (policy.isAllow()) {
                return PermissionResult.builder()
                        .allowed(true)
                        .userId(userId)
                        .roles(userRoles)
                        .permission(permission)
                        .policyId(policy.getPolicyId())
                        .build();
            } else {
                return PermissionResult.builder()
                        .allowed(false)
                        .reason("被策略 " + policy.getPolicyId() + " 拒绝")
                        .userId(userId)
                        .permission(permission)
                        .policyId(policy.getPolicyId())
                        .build();
            }
        }

        return PermissionResult.builder()
                .allowed(false)
                .reason("权限不足")
                .userId(userId)
                .roles(userRoles)
                .permission(permission)
                .build();
    }

    /**
     * 批量验证权限
     */
    public Map<String, PermissionResult> batchValidate(List<PermissionContext> contexts) {
        return contexts.stream()
                .collect(Collectors.toMap(
                        c -> c.getPermission() + ":" + c.getResourceId(),
                        this::validate
                ));
    }

    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(String userId, String role) {
        return getUserRoles(userId).contains(role);
    }

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(String userId, String permission) {
        Set<String> roles = getUserRoles(userId);
        for (String role : roles) {
            Set<String> perms = rolePermissions.get(role);
            if (perms != null && perms.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取用户权限列表
     */
    public Set<String> getUserPermissions(String userId) {
        Set<String> roles = getUserRoles(userId);
        Set<String> permissions = new HashSet<>();
        
        for (String role : roles) {
            Set<String> perms = rolePermissions.get(role);
            if (perms != null) {
                permissions.addAll(perms);
            }
        }
        
        return permissions;
    }

    /**
     * 添加权限策略
     */
    public void addPolicy(PermissionPolicy policy) {
        policies.put(policy.getPolicyId(), policy);
        log.info("Added permission policy: {}", policy.getPolicyId());
    }

    /**
     * 移除权限策略
     */
    public void removePolicy(String policyId) {
        policies.remove(policyId);
        log.info("Removed permission policy: {}", policyId);
    }

    /**
     * 为角色添加权限
     */
    public void addRolePermission(String role, String permission) {
        rolePermissions.computeIfAbsent(role, k -> new HashSet<>()).add(permission);
    }

    /**
     * 为角色移除权限
     */
    public void removeRolePermission(String role, String permission) {
        Set<String> perms = rolePermissions.get(role);
        if (perms != null) {
            perms.remove(permission);
        }
    }

    // ==================== 私有方法 ====================

    private Set<String> getUserRoles(String userId) {
        // 简化实现：从上下文或缓存获取
        // 实际应从用户服务获取
        if (userId.startsWith("admin:")) {
            return Set.of("ADMIN");
        } else if (userId.startsWith("user:")) {
            return Set.of("USER");
        } else if (userId.startsWith("viewer:")) {
            return Set.of("VIEWER");
        }
        return Set.of("GUEST");
    }

    private boolean validateResourceLevel(PermissionContext context, String role) {
        // 资源级别验证
        if (context.getResourceId() == null) {
            return true;
        }
        
        // 简化：资源拥有者总是可以访问
        if (context.getOwnerId() != null && 
            context.getOwnerId().equals(context.getUserId())) {
            return true;
        }
        
        // ADMIN可以访问所有资源
        if ("ADMIN".equals(role)) {
            return true;
        }
        
        return true;
    }

    private PermissionPolicy findApplicablePolicy(PermissionContext context) {
        for (PermissionPolicy policy : policies.values()) {
            if (policy.matches(context)) {
                return policy;
            }
        }
        return null;
    }

    // ==================== 数据类 ====================

    @Data
    @Builder
    public static class PermissionContext {
        private String userId;
        private String permission;
        private String resourceId;
        private String resourceType;
        private String ownerId;
        private Map<String, Object> attributes;
    }

    @Data
    @Builder
    public static class PermissionResult {
        private boolean allowed;
        private String reason;
        private String userId;
        private Set<String> roles;
        private String permission;
        private String resourceId;
        private String policyId;
    }

    @Data
    @Builder
    public static class PermissionPolicy {
        private String policyId;
        private String name;
        private String description;
        private List<String> allowedPermissions;
        private List<String> deniedPermissions;
        private List<String> allowedRoles;
        private List<String> deniedRoles;
        private List<String> allowedResources;
        private List<String> deniedResources;
        private boolean allow;
        private int priority;

        public boolean matches(PermissionContext context) {
            // 检查角色
            if (allowedRoles != null && !allowedRoles.isEmpty()) {
                boolean roleMatch = context.getUserId() != null && 
                        allowedRoles.stream().anyMatch(context.getUserId()::contains);
                if (!roleMatch) return false;
            }
            
            // 检查资源
            if (allowedResources != null && !allowedResources.isEmpty()) {
                boolean resourceMatch = context.getResourceId() != null &&
                        allowedResources.stream().anyMatch(context.getResourceId()::contains);
                if (!resourceMatch) return false;
            }
            
            return true;
        }

        public boolean isAllow() {
            return allow;
        }
    }
}