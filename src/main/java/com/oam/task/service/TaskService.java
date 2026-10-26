package com.oam.task.service;

import com.oam.common.security.SecurityUtil;
import com.oam.task.dto.*;
import com.oam.task.entity.Task;
import com.oam.task.entity.TaskProgress;
import com.oam.task.repository.TaskProgressRepository;
import com.oam.task.repository.TaskRepository;
import com.oam.user.entity.Role;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final List<String> STATUSES = Arrays.asList("TODO", "IN_PROGRESS", "DONE");
    private static final List<String> PRIORITIES = Arrays.asList("LOW", "NORMAL", "HIGH", "URGENT");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TaskRepository taskRepository;
    private final TaskProgressRepository progressRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       TaskProgressRepository progressRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    /** scope: assigned-我负责的（默认），created-我创建的，all-全部 */
    @Transactional(readOnly = true)
    public List<TaskDto> list(String scope, String status, String keyword) {
        User user = requireUser();
        String sc = scope == null ? "assigned" : scope.trim().toLowerCase();
        List<Task> tasks;
        if ("created".equals(sc)) {
            tasks = taskRepository.findByCreatorUserIdOrderByCreatedAtDesc(user.getId());
        } else if ("all".equals(sc)) {
            tasks = taskRepository.findAllByOrderByCreatedAtDesc();
        } else {
            tasks = taskRepository.findByAssigneeUserIdOrderByCreatedAtDesc(user.getId());
        }
        String st = normalizeStatus(status);
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        boolean admin = user.getRole() == Role.ADMIN;
        return tasks.stream()
                .filter(t -> st == null || st.equals(t.getStatus()))
                .filter(t -> kw.isEmpty()
                        || (t.getTitle() != null && t.getTitle().toLowerCase().contains(kw))
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(kw)))
                .map(t -> TaskDto.from(t, user.getId(), admin))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskStatsDto stats() {
        User user = requireUser();
        List<Task> tasks = taskRepository.findByAssigneeUserId(user.getId());
        TaskStatsDto dto = new TaskStatsDto();
        LocalDate today = LocalDate.now();
        for (Task t : tasks) {
            if ("TODO".equals(t.getStatus())) dto.setTodo(dto.getTodo() + 1);
            if ("IN_PROGRESS".equals(t.getStatus())) dto.setInProgress(dto.getInProgress() + 1);
            if ("DONE".equals(t.getStatus())) dto.setDone(dto.getDone() + 1);
            if (!"DONE".equals(t.getStatus()) && t.getDueDate() != null && t.getDueDate().isBefore(today)) {
                dto.setOverdue(dto.getOverdue() + 1);
            }
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public TaskDetailDto detail(Long id) {
        User user = requireUser();
        Task task = find(id);
        List<TaskProgressDto> records = progressRepository.findByTaskIdOrderByCreatedAtDesc(id).stream()
                .map(TaskProgressDto::from)
                .collect(Collectors.toList());
        return new TaskDetailDto(TaskDto.from(task, user.getId(), user.getRole() == Role.ADMIN), records);
    }

    @Transactional
    public TaskDto create(TaskRequest request) {
        User user = requireUser();
        User assignee = user;
        if (request.getAssigneeUserId() != null) {
            assignee = userRepository.findById(request.getAssigneeUserId())
                    .orElseThrow(() -> new IllegalArgumentException("负责人不存在"));
        }
        Task task = new Task();
        task.setTitle(request.getTitle().trim());
        task.setDescription(trimToNull(request.getDescription()));
        task.setAssigneeUserId(assignee.getId());
        task.setAssigneeName(displayName(assignee));
        task.setCreatorUserId(user.getId());
        task.setCreatorName(displayName(user));
        task.setPriority(normalizePriority(request.getPriority()));
        task.setDueDate(parseDate(request.getDueDate()));
        task.setStatus("TODO");
        task.setProgress(0);
        task.setCreatedBy(SecurityUtil.currentUsername());
        task.setUpdatedBy(SecurityUtil.currentUsername());
        task = taskRepository.save(task);
        addRecord(task, user, 0, "TODO",
                user.getId().equals(assignee.getId())
                        ? "创建任务"
                        : "创建任务并分派给 " + displayName(assignee));
        return TaskDto.from(task, user.getId(), user.getRole() == Role.ADMIN);
    }

    @Transactional
    public TaskDto update(Long id, TaskRequest request) {
        User user = requireUser();
        Task task = find(id);
        requireOwnerOrAdmin(task, user);
        task.setTitle(request.getTitle().trim());
        task.setDescription(trimToNull(request.getDescription()));
        task.setPriority(normalizePriority(request.getPriority()));
        task.setDueDate(parseDate(request.getDueDate()));
        task.setUpdatedBy(SecurityUtil.currentUsername());
        task = taskRepository.save(task);
        return TaskDto.from(task, user.getId(), user.getRole() == Role.ADMIN);
    }

    @Transactional
    public void delete(Long id) {
        User user = requireUser();
        Task task = find(id);
        requireOwnerOrAdmin(task, user);
        progressRepository.deleteByTaskId(id);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskDto assign(Long id, TaskAssignRequest request) {
        User user = requireUser();
        Task task = find(id);
        requireOwnerOrAdmin(task, user);
        if (request.getAssigneeUserId() == null) {
            throw new IllegalArgumentException("请选择负责人");
        }
        User assignee = userRepository.findById(request.getAssigneeUserId())
                .orElseThrow(() -> new IllegalArgumentException("负责人不存在"));
        task.setAssigneeUserId(assignee.getId());
        task.setAssigneeName(displayName(assignee));
        task.setUpdatedBy(SecurityUtil.currentUsername());
        task = taskRepository.save(task);
        addRecord(task, user, task.getProgress(), task.getStatus(),
                "已将任务分派给 " + displayName(assignee));
        return TaskDto.from(task, user.getId(), user.getRole() == Role.ADMIN);
    }

    @Transactional
    public TaskDetailDto updateProgress(Long id, TaskProgressRequest request) {
        User user = requireUser();
        Task task = find(id);
        boolean assignee = user.getId().equals(task.getAssigneeUserId());
        if (!assignee && user.getRole() != Role.ADMIN && !user.getId().equals(task.getCreatorUserId())) {
            throw new IllegalArgumentException("只有负责人、创建人或管理员可以更新进度");
        }
        int progress = request.getProgress();
        String status = normalizeStatus(request.getStatus());
        if (progress >= 100) {
            status = "DONE";
        } else if ("DONE".equals(status)) {
            progress = 100;
        } else if (status == null) {
            status = progress > 0 ? "IN_PROGRESS" : "TODO";
        }
        task.setProgress(progress);
        task.setStatus(status);
        task.setCompletedAt("DONE".equals(status) ? LocalDateTime.now() : null);
        task.setUpdatedBy(SecurityUtil.currentUsername());
        task = taskRepository.save(task);
        addRecord(task, user, progress, status, trimToNull(request.getNote()));
        return detail(id);
    }

    // ==================== 辅助 ====================

    private void addRecord(Task task, User operator, int progress, String status, String note) {
        TaskProgress record = new TaskProgress();
        record.setTaskId(task.getId());
        record.setOperatorUserId(operator.getId());
        record.setOperatorName(displayName(operator));
        record.setProgress(progress);
        record.setStatus(status);
        record.setNote(note);
        progressRepository.save(record);
    }

    private Task find(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
    }

    private void requireOwnerOrAdmin(Task task, User user) {
        if (user.getRole() != Role.ADMIN && !user.getId().equals(task.getCreatorUserId())) {
            throw new IllegalArgumentException("只有任务创建人或管理员可以执行该操作");
        }
    }

    private User requireUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private String displayName(User user) {
        String nickname = user.getNickname();
        return nickname == null || nickname.trim().isEmpty() ? user.getUsername() : nickname;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        String value = status.trim().toUpperCase();
        if (!STATUSES.contains(value)) {
            throw new IllegalArgumentException("任务状态不正确");
        }
        return value;
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) return "NORMAL";
        String value = priority.trim().toUpperCase();
        if (!PRIORITIES.contains(value)) {
            throw new IllegalArgumentException("优先级不正确");
        }
        return value;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim(), DATE_FMT);
        } catch (Exception e) {
            throw new IllegalArgumentException("截止日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
