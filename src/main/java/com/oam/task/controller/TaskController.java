package com.oam.task.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.task.dto.*;
import com.oam.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/api/tasks")
    public ApiResponse<List<TaskDto>> list(@RequestParam(required = false) String scope,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(taskService.list(scope, status, keyword));
    }

    @GetMapping("/api/tasks/stats")
    public ApiResponse<TaskStatsDto> stats() {
        return ApiResponse.ok(taskService.stats());
    }

    @GetMapping("/api/tasks/{id}")
    public ApiResponse<TaskDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(taskService.detail(id));
    }

    @PostMapping("/api/tasks")
    public ApiResponse<TaskDto> create(@Valid @RequestBody TaskRequest request) {
        return ApiResponse.ok("任务已创建", taskService.create(request));
    }

    @PutMapping("/api/tasks/{id}")
    public ApiResponse<TaskDto> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ApiResponse.ok("任务已更新", taskService.update(id, request));
    }

    @DeleteMapping("/api/tasks/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ApiResponse.ok("任务已删除", null);
    }

    @PostMapping("/api/tasks/{id}/progress")
    public ApiResponse<TaskDetailDto> updateProgress(@PathVariable Long id,
                                                     @Valid @RequestBody TaskProgressRequest request) {
        return ApiResponse.ok("进度已更新", taskService.updateProgress(id, request));
    }

    @PostMapping("/api/tasks/{id}/assign")
    public ApiResponse<TaskDto> assign(@PathVariable Long id, @RequestBody TaskAssignRequest request) {
        return ApiResponse.ok("任务已分派", taskService.assign(id, request));
    }
}
