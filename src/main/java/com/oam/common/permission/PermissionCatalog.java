package com.oam.common.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 权限功能树目录：集中定义系统中的功能模块及其操作权限码。
 * 权限码格式为 {@code 模块:操作}，如 user:view。
 * 后续新增业务模块（如公告、审批、日程等）时在此登记。
 */
public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    /** 功能模块节点（可含子模块，用于按界面菜单层级展示） */
    public static class Module {
        private String module;
        private String name;
        private List<Operation> operations;
        private List<Module> children;

        public Module(String module, String name, List<Operation> operations, Module... children) {
            this.module = module;
            this.name = name;
            this.operations = operations;
            this.children = children == null || children.length == 0 ? null : Arrays.asList(children);
        }

        public String getModule() {
            return module;
        }

        public String getName() {
            return name;
        }

        public List<Operation> getOperations() {
            return operations;
        }

        public List<Module> getChildren() {
            return children;
        }
    }

    /** 模块下的操作节点 */
    public static class Operation {
        private String code;
        private String name;

        public Operation(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    private static final List<Module> MODULES = buildModules();

    private static List<Module> buildModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(new Module("user", "账号管理", op(
                "view", "查看",
                "create", "新增",
                "update", "编辑",
                "resetpassword", "重置密码",
                "delete", "删除",
                "export", "导出")));
        return modules;
    }

    private static List<Operation> op(String... kv) {
        List<Operation> list = new ArrayList<>();
        for (int i = 0; i < kv.length; i += 2) {
            list.add(new Operation(kv[i], kv[i + 1]));
        }
        return list;
    }

    /** 功能树（供权限管理界面展示） */
    public static List<Module> tree() {
        return MODULES;
    }

    /** 所有权限码（管理员默认拥有） */
    public static List<String> allCodes() {
        List<String> list = new ArrayList<>();
        for (Module module : MODULES) {
            collectCodes(module, list);
        }
        return list;
    }

    private static void collectCodes(Module module, List<String> list) {
        if (module.getChildren() != null) {
            for (Module child : module.getChildren()) {
                collectCodes(child, list);
            }
        }
        if (module.getOperations() != null) {
            for (Operation operation : module.getOperations()) {
                list.add(module.getModule() + ":" + operation.getCode());
            }
        }
    }

    /** 校验权限码是否合法 */
    public static boolean contains(String code) {
        return allCodes().contains(code);
    }
}
