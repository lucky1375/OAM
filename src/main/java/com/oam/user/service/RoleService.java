package com.oam.user.service;

import com.oam.common.permission.PermissionCatalog;
import com.oam.user.entity.Role;
import com.oam.user.entity.RolePermission;
import com.oam.user.repository.RolePermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 按角色粒度管理权限配置。
 * ADMIN 默认拥有全部权限；USER 默认只含工作台 + 查看类权限。
 */
@Service
public class RoleService {

    private final RolePermissionRepository rolePermissionRepository;

    public RoleService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public static final Set<String> ADMIN_DEFAULT_PERMISSIONS = new HashSet<>();

    /** 普通用户的默认权限（工作台基础能力 + 各模块 view） */
    public static final Set<String> USER_DEFAULT_PERMISSIONS;
    static {
        Set<String> s = new LinkedHashSet<>();
        // 工作台相关
        s.add("workbench:view");
        s.add("attendance:view");
        s.add("attendance:checkin");
        s.add("suggestion:view");
        s.add("suggestion:create");
        s.add("suggestion:reply");
        // 审批 - 流程：发起/查看自己的
        s.add("approval:view");
        s.add("workflow:view");
        s.add("workflow:submit");
        s.add("workflow:approve");
        // 报销
        s.add("expense:view");
        s.add("expense:create");
        s.add("expense:submit");
        // 协作
        s.add("collaboration:view");
        s.add("forum:view");
        s.add("forum:create");
        s.add("forum:reply");
        s.add("forum:like");
        s.add("announcement:view");
        // 日程会议
        s.add("schedule:view");
        s.add("calendar:view");
        s.add("calendar:create");
        s.add("calendar:update");
        s.add("calendar:delete");
        s.add("meeting:view");
        s.add("meeting:create");
        s.add("meeting:update");
        // 任务
        s.add("task:view");
        s.add("task:create");
        s.add("task:update");
        s.add("task:progress");
        // 组织架构 - 只读
        s.add("organization:view");
        s.add("department:view");
        s.add("position:view");
        s.add("personnel:view");
        // 系统管理 - 只读查看
        s.add("system:view");
        s.add("role:view");
        USER_DEFAULT_PERMISSIONS = Collections.unmodifiableSet(s);
    }

    /** 角色当前已配置的权限码集合 */
    public List<String> listByRole(Role role) {
        if (role == Role.ADMIN) {
            return PermissionCatalog.allCodes();
        }
        return rolePermissionRepository.findByRole(role).stream()
                .map(RolePermission::getPermissionCode)
                .collect(Collectors.toList());
    }

    @Transactional
    public void configureRole(Role role, List<String> codes) {
        if (role == Role.ADMIN) {
            // ADMIN 始终默认全部权限，不单独存
            return;
        }
        Set<String> toAssign = new LinkedHashSet<>(codes == null ? Collections.emptyList() : codes);
        for (String c : toAssign) {
            if (!PermissionCatalog.contains(c)) {
                throw new IllegalArgumentException("无效权限码：" + c);
            }
        }
        rolePermissionRepository.deleteByRole(role);
        for (String c : toAssign) {
            RolePermission rp = new RolePermission();
            rp.setRole(role);
            rp.setPermissionCode(c);
            rolePermissionRepository.save(rp);
        }
    }

    @Transactional
    public void seedDefaults() {
        // 每次启动都把 USER 角色权限重置为常量（幂等，保证常量改了就同步）
        rolePermissionRepository.deleteByRole(Role.USER);
        for (String c : USER_DEFAULT_PERMISSIONS) {
            RolePermission rp = new RolePermission();
            rp.setRole(Role.USER);
            rp.setPermissionCode(c);
            rolePermissionRepository.save(rp);
        }
    }
}
