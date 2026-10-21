package com.oam.workflow.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.workflow.dto.*;
import com.oam.workflow.service.ApprovalService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/api/workflows")
    public ApiResponse<List<ApprovalFlowDto>> list(@RequestParam(required = false) String scope,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String type) {
        return ApiResponse.ok(approvalService.list(scope, keyword, status, type));
    }

    @GetMapping("/api/workflows/stats")
    public ApiResponse<ApprovalStatsDto> stats() {
        return ApiResponse.ok(approvalService.stats());
    }

    @GetMapping("/api/workflows/{id}")
    public ApiResponse<ApprovalFlowDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(approvalService.detail(id));
    }

    @PostMapping("/api/workflows")
    public ApiResponse<ApprovalFlowDto> submit(@Valid @RequestBody ApprovalSubmitRequest request) {
        return ApiResponse.ok("流程已提交", approvalService.submit(request));
    }

    @PostMapping("/api/workflows/{id}/approve")
    public ApiResponse<ApprovalFlowDto> approve(@PathVariable Long id,
                                                @RequestBody(required = false) ApprovalActionRequest request) {
        return ApiResponse.ok("审批通过", approvalService.approve(id, request));
    }

    @PostMapping("/api/workflows/{id}/reject")
    public ApiResponse<ApprovalFlowDto> reject(@PathVariable Long id,
                                               @RequestBody(required = false) ApprovalActionRequest request) {
        return ApiResponse.ok("已驳回", approvalService.reject(id, request));
    }

    @PostMapping("/api/workflows/{id}/delegate")
    public ApiResponse<ApprovalFlowDto> delegate(@PathVariable Long id,
                                                 @Valid @RequestBody ApprovalDelegateRequest request) {
        return ApiResponse.ok("已转办", approvalService.delegate(id, request));
    }

    @PostMapping("/api/workflows/{id}/cancel")
    public ApiResponse<ApprovalFlowDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok("已撤回", approvalService.cancel(id));
    }

    // ==================== 固定审批节点配置 ====================

    @GetMapping("/api/workflow-nodes")
    public ApiResponse<List<ProcessNodeDto>> listNodes(@RequestParam String type) {
        return ApiResponse.ok(approvalService.listNodes(type));
    }

    @PutMapping("/api/workflow-nodes/{type}")
    public ApiResponse<List<ProcessNodeDto>> saveNodes(@PathVariable String type,
                                                       @RequestBody(required = false) List<ProcessNodeRequest> requests) {
        return ApiResponse.ok("流程节点已保存", approvalService.saveNodes(type, requests));
    }
}
