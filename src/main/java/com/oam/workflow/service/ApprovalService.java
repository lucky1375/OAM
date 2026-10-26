package com.oam.workflow.service;

import com.oam.calendar.entity.CalendarEvent;
import com.oam.calendar.repository.CalendarRepository;
import com.oam.common.security.SecurityUtil;
import com.oam.user.entity.Role;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import com.oam.workflow.dto.*;
import com.oam.workflow.entity.ApprovalFlow;
import com.oam.workflow.entity.ApprovalRecord;
import com.oam.workflow.entity.ProcessNode;
import com.oam.workflow.event.ApprovalFinishedEvent;
import com.oam.workflow.repository.ApprovalFlowRepository;
import com.oam.workflow.repository.ApprovalRecordRepository;
import com.oam.workflow.repository.ProcessNodeRepository;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private static final List<String> TYPES = Arrays.asList("LEAVE", "EXPENSE", "PURCHASE", "PATCH", "OTHER");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** BPMN 流程定义 key，与 processes/oamApproval.bpmn20.xml 保持一致 */
    private static final String PROCESS_KEY = "oamApproval";
    private static final String BPMN_RESOURCE = "processes/oamApproval.bpmn20.xml";

    private final ApprovalFlowRepository flowRepository;
    private final ApprovalRecordRepository recordRepository;
    private final ProcessNodeRepository processNodeRepository;
    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final ApplicationEventPublisher eventPublisher;

    public ApprovalService(ApprovalFlowRepository flowRepository,
                           ApprovalRecordRepository recordRepository,
                           ProcessNodeRepository processNodeRepository,
                           UserRepository userRepository,
                           CalendarRepository calendarRepository,
                           RuntimeService runtimeService,
                           TaskService taskService,
                           RepositoryService repositoryService,
                           ApplicationEventPublisher eventPublisher) {
        this.flowRepository = flowRepository;
        this.recordRepository = recordRepository;
        this.processNodeRepository = processNodeRepository;
        this.userRepository = userRepository;
        this.calendarRepository = calendarRepository;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.eventPublisher = eventPublisher;
    }

    /** scope: mine-我发起的，todo-待我审批，done-我已处理 */
    @Transactional(readOnly = true)
    public List<ApprovalFlowDto> list(String scope, String keyword, String status, String type) {
        User user = currentUser();
        if (user == null) {
            throw new IllegalArgumentException("未登录");
        }
        String sc = scope == null ? "mine" : scope.trim().toLowerCase();
        List<ApprovalFlow> flows;
        if ("todo".equals(sc)) {
            flows = flowRepository.findByApproverUserIdAndStatusOrderByCreatedAtDesc(user.getId(), "PENDING");
        } else if ("done".equals(sc)) {
            Set<Long> flowIds = recordRepository.findByOperatorUserIdOrderByCreatedAtDesc(user.getId()).stream()
                    .filter(r -> !"SUBMIT".equals(r.getAction()) && !"CANCEL".equals(r.getAction()))
                    .map(ApprovalRecord::getFlowId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            flows = new ArrayList<>();
            for (Long fid : flowIds) {
                flowRepository.findById(fid).ifPresent(flows::add);
            }
        } else {
            flows = flowRepository.findByApplicantUserIdOrderByCreatedAtDesc(user.getId());
        }
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        String st = normalizeStatus(status);
        String tp = normalizeType(type);
        return flows.stream()
                .filter(f -> kw.isEmpty()
                        || (f.getTitle() != null && f.getTitle().toLowerCase().contains(kw))
                        || (f.getContent() != null && f.getContent().toLowerCase().contains(kw)))
                .filter(f -> st == null || st.equals(f.getStatus()))
                .filter(f -> tp == null || tp.equals(f.getType()))
                .map(f -> ApprovalFlowDto.from(f, user.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApprovalFlowDetailDto detail(Long id) {
        ApprovalFlow f = find(id);
        User user = currentUser();
        if (user == null) {
            throw new IllegalArgumentException("未登录");
        }
        boolean allowed = user.getRole() == Role.ADMIN
                || user.getId().equals(f.getApplicantUserId())
                || user.getId().equals(f.getApproverUserId())
                || recordRepository.findByFlowIdOrderByCreatedAtAsc(id).stream()
                        .anyMatch(r -> user.getId().equals(r.getOperatorUserId()));
        if (!allowed) {
            throw new IllegalArgumentException("无权查看该流程");
        }
        List<ApprovalRecordDto> records = recordRepository.findByFlowIdOrderByCreatedAtAsc(id).stream()
                .map(ApprovalRecordDto::from)
                .collect(Collectors.toList());
        return new ApprovalFlowDetailDto(ApprovalFlowDto.from(f, user.getId()), records);
    }

    @Transactional
    public ApprovalFlowDto submit(ApprovalSubmitRequest request) {
        User user = requireUser();
        String type = normalizeType(request.getType()) == null ? "OTHER" : normalizeType(request.getType());

        // 优先使用该流程类型配置的固定审批节点
        List<ProcessNode> nodes = processNodeRepository.findByTypeOrderByNodeOrderAsc(type);
        List<User> approvers = new ArrayList<>();
        List<String> nodeNames = new ArrayList<>();
        if (!nodes.isEmpty()) {
            for (ProcessNode node : nodes) {
                User approver = userRepository.findById(node.getApproverUserId())
                        .orElseThrow(() -> new IllegalArgumentException("节点「" + node.getNodeName() + "」的审批人不存在"));
                approvers.add(approver);
                nodeNames.add(node.getNodeName());
            }
        } else {
            if (request.getApproverUserId() == null) {
                throw new IllegalArgumentException("请选择审批人，或先为该流程类型配置固定审批节点");
            }
            User approver = userRepository.findById(request.getApproverUserId())
                    .orElseThrow(() -> new IllegalArgumentException("审批人不存在"));
            approvers.add(approver);
            nodeNames.add("审批");
        }

        ApprovalFlow f = new ApprovalFlow();
        f.setType(type);
        f.setTitle(request.getTitle().trim());
        f.setContent(request.getContent().trim());

        // 请假流程必须选择请假时间段
        LocalDateTime leaveStart = parseTime(request.getStartTime());
        LocalDateTime leaveEnd = parseTime(request.getEndTime());
        if ("LEAVE".equals(type)) {
            if (leaveStart == null || leaveEnd == null) {
                throw new IllegalArgumentException("请选择请假开始和结束时间");
            }
            if (!leaveEnd.isAfter(leaveStart)) {
                throw new IllegalArgumentException("请假结束时间必须晚于开始时间");
            }
        }
        f.setLeaveStartTime(leaveStart);
        f.setLeaveEndTime(leaveEnd);

        f.setApplicantUserId(user.getId());
        f.setApplicantName(displayName(user));
        f.setApproverUserId(approvers.get(0).getId());
        f.setApproverName(displayName(approvers.get(0)));
        f.setStatus("PENDING");
        f.setNodeIndex(0);
        f.setTotalNodes(approvers.size());
        f.setNodeSequence(String.join(",", nodeNames));
        f.setApproverSequence(approvers.stream().map(User::getUsername).collect(Collectors.joining(",")));
        f.setCurrentNodeName(nodeNames.get(0));
        f.setCreatedBy(SecurityUtil.currentUsername());
        f = flowRepository.save(f);

        ensureProcessDeployed();
        Map<String, Object> vars = new HashMap<>();
        vars.put("approvers", approvers.stream().map(User::getUsername).collect(Collectors.toList()));
        vars.put("applicant", user.getUsername());
        vars.put("flowId", f.getId());
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                PROCESS_KEY, String.valueOf(f.getId()), vars);
        f.setProcessInstanceId(instance.getId());
        f = flowRepository.save(f);

        addRecord(f.getId(), "SUBMIT", user, null,
                request.getTitle().trim() + "（共" + approvers.size() + "级审批）");
        return ApprovalFlowDto.from(f, user.getId());
    }

    @Transactional
    public ApprovalFlowDto approve(Long id, ApprovalActionRequest request) {
        ApprovalFlow f = findPendingForApprover(id);
        User user = requireUser();
        completeTask(f, true, request == null ? null : request.getComment());

        int current = f.getNodeIndex() == null ? 0 : f.getNodeIndex();
        int total = f.getTotalNodes() == null ? 1 : f.getTotalNodes();
        int next = current + 1;
        if (next < total && f.getApproverSequence() != null) {
            // 还有后续固定节点：流转给下一级审批人
            String[] usernames = f.getApproverSequence().split(",");
            String[] names = f.getNodeSequence() == null ? new String[0] : f.getNodeSequence().split(",");
            if (next >= usernames.length) {
                throw new IllegalArgumentException("流程节点配置与提交时快照不一致");
            }
            User nextApprover = userRepository.findByUsername(usernames[next])
                    .orElseThrow(() -> new IllegalArgumentException("下一级审批人不存在"));
            f.setNodeIndex(next);
            f.setApproverUserId(nextApprover.getId());
            f.setApproverName(displayName(nextApprover));
            f.setCurrentNodeName(next < names.length ? names[next] : "审批");
            f.setStatus("PENDING");
        } else {
            f.setStatus("APPROVED");
            f.setFinishedAt(LocalDateTime.now());
            // 请假审批通过后，将请假时间段写入申请人的日历
            syncLeaveToCalendar(f);
        }
        f.setUpdatedBy(SecurityUtil.currentUsername());
        f = flowRepository.save(f);
        addRecord(id, "APPROVE", user, null, request == null ? null : request.getComment());
        if ("APPROVED".equals(f.getStatus())) {
            eventPublisher.publishEvent(new ApprovalFinishedEvent(f.getId(), "APPROVED",
                    displayName(user), request == null ? null : request.getComment()));
        }
        return ApprovalFlowDto.from(f, user.getId());
    }

    @Transactional
    public ApprovalFlowDto reject(Long id, ApprovalActionRequest request) {
        ApprovalFlow f = findPendingForApprover(id);
        User user = requireUser();
        completeTask(f, false, request == null ? null : request.getComment());
        f.setStatus("REJECTED");
        f.setFinishedAt(LocalDateTime.now());
        f.setUpdatedBy(SecurityUtil.currentUsername());
        f = flowRepository.save(f);
        addRecord(id, "REJECT", user, null, request == null ? null : request.getComment());
        eventPublisher.publishEvent(new ApprovalFinishedEvent(f.getId(), "REJECTED",
                displayName(user), request == null ? null : request.getComment()));
        return ApprovalFlowDto.from(f, user.getId());
    }

    /** 转办：把当前审批人改为目标人 */
    @Transactional
    public ApprovalFlowDto delegate(Long id, ApprovalDelegateRequest request) {
        ApprovalFlow f = findPendingForApprover(id);
        User user = requireUser();
        User target = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new IllegalArgumentException("转办人不存在"));
        if (target.getId().equals(user.getId())) {
            throw new IllegalArgumentException("不能转办给自己");
        }
        f.setApproverUserId(target.getId());
        f.setApproverName(displayName(target));
        // 同步流程快照中当前节点的审批人，避免后续流转错乱
        int idx = f.getNodeIndex() == null ? 0 : f.getNodeIndex();
        if (f.getApproverSequence() != null) {
            String[] usernames = f.getApproverSequence().split(",");
            if (idx < usernames.length) {
                usernames[idx] = target.getUsername();
                f.setApproverSequence(String.join(",", usernames));
            }
        }
        f.setUpdatedBy(SecurityUtil.currentUsername());
        f = flowRepository.save(f);
        // 同步修改 Activiti 当前任务的办理人
        Task task = currentTask(f);
        if (task != null) {
            taskService.setAssignee(task.getId(), target.getUsername());
        }
        addRecord(id, "DELEGATE", user, target, request.getComment());
        return ApprovalFlowDto.from(f, user.getId());
    }

    /** 撤回：仅申请人且仍在审批中 */
    @Transactional
    public ApprovalFlowDto cancel(Long id) {
        ApprovalFlow f = find(id);
        User user = requireUser();
        if (!user.getId().equals(f.getApplicantUserId())) {
            throw new IllegalArgumentException("只能撤回自己发起的流程");
        }
        if (!"PENDING".equals(f.getStatus())) {
            throw new IllegalArgumentException("该流程已结束，无法撤回");
        }
        // 结束 Activiti 流程实例
        if (f.getProcessInstanceId() != null
                && runtimeService.createProcessInstanceQuery()
                        .processInstanceId(f.getProcessInstanceId()).count() > 0) {
            runtimeService.deleteProcessInstance(f.getProcessInstanceId(), "申请人撤回");
        }
        f.setStatus("CANCELLED");
        f.setFinishedAt(LocalDateTime.now());
        f.setUpdatedBy(SecurityUtil.currentUsername());
        f = flowRepository.save(f);
        addRecord(id, "CANCEL", user, null, "申请人撤回");
        eventPublisher.publishEvent(new ApprovalFinishedEvent(f.getId(), "CANCELLED",
                displayName(user), "申请人撤回"));
        return ApprovalFlowDto.from(f, user.getId());
    }

    // ==================== 固定审批节点配置 ====================

    @Transactional(readOnly = true)
    public List<ProcessNodeDto> listNodes(String type) {
        String t = normalizeType(type);
        if (t == null) {
            throw new IllegalArgumentException("流程类型不正确");
        }
        return processNodeRepository.findByTypeOrderByNodeOrderAsc(t).stream()
                .map(ProcessNodeDto::from)
                .collect(Collectors.toList());
    }

    /** 整体保存某流程类型的固定审批节点（按传入顺序排列），仅管理员可操作 */
    @Transactional
    public List<ProcessNodeDto> saveNodes(String type, List<ProcessNodeRequest> requests) {
        User user = requireUser();
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("只有管理员可以配置固定审批节点");
        }
        String t = normalizeType(type);
        if (t == null) {
            throw new IllegalArgumentException("流程类型不正确");
        }
        if (requests != null && requests.size() > 10) {
            throw new IllegalArgumentException("最多配置 10 个审批节点");
        }
        List<ProcessNode> nodes = new ArrayList<>();
        if (requests != null) {
            int order = 0;
            for (ProcessNodeRequest request : requests) {
                if (request.getNodeName() == null || request.getNodeName().trim().isEmpty()) {
                    throw new IllegalArgumentException("请填写节点名称");
                }
                if (request.getApproverUserId() == null) {
                    throw new IllegalArgumentException("请选择节点审批人");
                }
                User approver = userRepository.findById(request.getApproverUserId())
                        .orElseThrow(() -> new IllegalArgumentException("审批人不存在"));
                ProcessNode node = new ProcessNode();
                node.setType(t);
                node.setNodeOrder(order++);
                node.setNodeName(request.getNodeName().trim());
                node.setApproverUserId(approver.getId());
                node.setApproverName(displayName(approver));
                node.setCreatedBy(SecurityUtil.currentUsername());
                nodes.add(node);
            }
        }
        processNodeRepository.deleteByType(t);
        processNodeRepository.flush();
        return processNodeRepository.saveAll(nodes).stream()
                .map(ProcessNodeDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApprovalStatsDto stats() {
        User user = currentUser();
        if (user == null) {
            throw new IllegalArgumentException("未登录");
        }
        ApprovalStatsDto dto = new ApprovalStatsDto();
        dto.setMyPending(flowRepository.countByApplicantUserIdAndStatus(user.getId(), "PENDING"));
        dto.setTodo(flowRepository.countByApproverUserIdAndStatus(user.getId(), "PENDING"));
        dto.setApproved(flowRepository.countByApplicantUserIdAndStatus(user.getId(), "APPROVED"));
        dto.setRejected(flowRepository.countByApplicantUserIdAndStatus(user.getId(), "REJECTED"));
        return dto;
    }

    // ==================== 辅助 ====================

    private LocalDateTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), TIME_FMT);
        } catch (Exception e) {
            throw new IllegalArgumentException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    /** 请假审批通过后生成申请人的日历日程；重复调用不会重复生成 */
    private void syncLeaveToCalendar(ApprovalFlow f) {
        if (!"LEAVE".equals(f.getType())
                || f.getLeaveStartTime() == null || f.getLeaveEndTime() == null) {
            return;
        }
        if (!calendarRepository.findBySourceFlowId(f.getId()).isEmpty()) {
            return;
        }
        String title = "请假：" + f.getTitle();
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title.length() > 100 ? title.substring(0, 100) : title);
        event.setStartTime(f.getLeaveStartTime());
        event.setEndTime(f.getLeaveEndTime());
        event.setAllDay(false);
        event.setRemark("请假审批通过（" + String.format("WF%06d", f.getId()) + "）");
        event.setOwnerUserId(f.getApplicantUserId());
        event.setSourceFlowId(f.getId());
        event.setCreatedBy(f.getCreatedBy());
        calendarRepository.save(event);
    }

    /** 完成当前审批任务，approved 变量驱动 BPMN 排他网关 */
    private void completeTask(ApprovalFlow f, boolean approved, String comment) {
        if (f.getProcessInstanceId() == null) {
            // 未接入 Activiti 的历史流程，直接由业务状态流转
            return;
        }
        Task task = currentTask(f);
        if (task == null) {
            throw new IllegalArgumentException("流程已结束或当前无待办任务");
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", approved);
        if (comment != null && !comment.trim().isEmpty()) {
            vars.put("comment", comment.trim());
        }
        taskService.complete(task.getId(), vars);
    }

    private Task currentTask(ApprovalFlow f) {
        if (f.getProcessInstanceId() == null) {
            return null;
        }
        return taskService.createTaskQuery()
                .processInstanceId(f.getProcessInstanceId())
                .singleResult();
    }

    /** 确保 BPMN 流程定义已部署（自动部署失败时的兜底） */
    private void ensureProcessDeployed() {
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS_KEY)
                .count();
        if (count == 0) {
            repositoryService.createDeployment()
                    .name("OAM 审批流程")
                    .addClasspathResource(BPMN_RESOURCE)
                    .deploy();
        }
    }

    private ApprovalFlow find(Long id) {
        return flowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("流程不存在"));
    }

    private ApprovalFlow findPendingForApprover(Long id) {
        ApprovalFlow f = find(id);
        User user = requireUser();
        if (!"PENDING".equals(f.getStatus())) {
            throw new IllegalArgumentException("该流程已结束，无法操作");
        }
        if (!user.getId().equals(f.getApproverUserId())) {
            throw new IllegalArgumentException("您不是该流程的当前审批人");
        }
        return f;
    }

    private void addRecord(Long flowId, String action, User operator, User target, String comment) {
        ApprovalRecord r = new ApprovalRecord();
        r.setFlowId(flowId);
        r.setAction(action);
        r.setOperatorUserId(operator.getId());
        r.setOperatorName(displayName(operator));
        if (target != null) {
            r.setTargetUserId(target.getId());
            r.setTargetName(displayName(target));
        }
        r.setComment(comment);
        recordRepository.save(r);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        String s = status.trim().toUpperCase();
        return Arrays.asList("PENDING", "APPROVED", "REJECTED", "CANCELLED").contains(s) ? s : null;
    }

    private String normalizeType(String type) {
        if (type == null || type.trim().isEmpty()) return null;
        String t = type.trim().toUpperCase();
        return TYPES.contains(t) ? t : null;
    }

    private User requireUser() {
        User u = currentUser();
        if (u == null) {
            throw new IllegalArgumentException("未登录");
        }
        return u;
    }

    private User currentUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    private String displayName(User u) {
        return u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername();
    }
}
