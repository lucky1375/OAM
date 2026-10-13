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

        // 工作台
        modules.add(new Module("workbench", "工作台", null,
                new Module("attendance", "考勤打卡", op(
                        "view", "查看",
                        "checkin", "打卡",
                        "update", "补卡",
                        "export", "导出")),
                new Module("suggestion", "建议反馈", op(
                        "view", "查看",
                        "create", "提交",
                        "update", "编辑",
                        "reply", "回复",
                        "delete", "删除"))));

        // 审批管理（流程引擎 Activiti）
        modules.add(new Module("approval", "审批管理", null,
                new Module("workflow", "流程审批", op(
                        "view", "查看",
                        "submit", "发起",
                        "approve", "审批",
                        "delegate", "转办")),
                new Module("expense", "报销管理", op(
                        "view", "查看",
                        "create", "新增",
                        "update", "编辑",
                        "delete", "删除",
                        "submit", "提交审批",
                        "invoice", "发票识别"))));

        // 协作交流
        modules.add(new Module("collaboration", "协作交流", null,
                new Module("forum", "企业论坛", op(
                        "view", "查看",
                        "create", "发帖",
                        "update", "编辑",
                        "delete", "删除",
                        "reply", "评论",
                        "like", "点赞")),
                new Module("announcement", "公告通知", op(
                        "view", "查看",
                        "create", "发布",
                        "update", "编辑",
                        "delete", "删除"))));

        // 日程会议
        modules.add(new Module("schedule", "日程会议", null,
                new Module("calendar", "日程安排", op(
                        "view", "查看",
                        "create", "新增",
                        "update", "编辑",
                        "delete", "删除")),
                new Module("meeting", "会议管理", op(
                        "view", "查看",
                        "create", "新增",
                        "update", "编辑",
                        "delete", "删除",
                        "room", "会议室预定"))));

        // 任务协作
        modules.add(new Module("task", "任务协作", op(
                "view", "查看",
                "create", "新增",
                "update", "编辑",
                "delete", "删除",
                "assign", "分派",
                "progress", "进度更新")));

        // 组织架构
        modules.add(new Module("organization", "组织架构", op("view", "查看"),
                new Module("department", "部门管理", op(
                        "view", "查看",
                        "create", "新增",
                        "update", "编辑",
                        "delete", "删除")),
                new Module("position", "岗位管理", op(
                        "view", "查看",
                        "create", "新增",
                        "update", "编辑",
                        "delete", "删除")),
                new Module("personnel", "人员管理", op(
                        "view", "查看",
                        "create", "新增",
                        "update", "编辑",
                        "delete", "删除"))));

        // 系统管理
        modules.add(new Module("system", "系统管理", op("view", "查看"),
                new Module("user", "账号管理", op(
                        "view", "查看",
                        "create", "新增",
                        "update", "编辑",
                        "resetpassword", "重置密码",
                        "delete", "删除",
                        "export", "导出")),
                new Module("role", "角色权限", op(
                        "view", "查看",
                        "update", "配置"))));

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
