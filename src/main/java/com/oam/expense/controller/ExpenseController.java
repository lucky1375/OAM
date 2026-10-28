package com.oam.expense.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.expense.dto.*;
import com.oam.expense.service.ExpenseService;
import com.oam.expense.service.InvoiceOcrService;
import com.oam.workflow.dto.ApprovalActionRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ExpenseController {

    private final ExpenseService expenseService;
    private final InvoiceOcrService ocrService;

    public ExpenseController(ExpenseService expenseService, InvoiceOcrService ocrService) {
        this.expenseService = expenseService;
        this.ocrService = ocrService;
    }

    @GetMapping("/api/expenses")
    public ApiResponse<List<ExpenseClaimDto>> list(@RequestParam(required = false) String scope,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String category) {
        return ApiResponse.ok(expenseService.list(scope, keyword, status, category));
    }

    @GetMapping("/api/expenses/stats")
    public ApiResponse<ExpenseStatsDto> stats() {
        return ApiResponse.ok(expenseService.stats());
    }

    @GetMapping("/api/expenses/ocr-status")
    public ApiResponse<OcrStatusDto> ocrStatus() {
        return ApiResponse.ok(new OcrStatusDto(ocrService.isEnabled(), ocrService.provider()));
    }

    @GetMapping("/api/expenses/{id}")
    public ApiResponse<ExpenseClaimDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(expenseService.detail(id));
    }

    @PostMapping("/api/expenses")
    public ApiResponse<ExpenseClaimDto> create(@Valid @RequestBody ExpenseClaimRequest request) {
        return ApiResponse.ok("报销单已保存", expenseService.create(request));
    }

    @PutMapping("/api/expenses/{id}")
    public ApiResponse<ExpenseClaimDto> update(@PathVariable Long id,
                                               @Valid @RequestBody ExpenseClaimRequest request) {
        return ApiResponse.ok("报销单已更新", expenseService.update(id, request));
    }

    @DeleteMapping("/api/expenses/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/api/expenses/{id}/submit")
    public ApiResponse<ExpenseClaimDto> submit(@PathVariable Long id,
                                               @RequestBody(required = false) ExpenseSubmitRequest request) {
        return ApiResponse.ok("报销单已提交审批", expenseService.submit(id, request));
    }

    @PostMapping("/api/expenses/{id}/cancel")
    public ApiResponse<ExpenseClaimDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok("已撤回", expenseService.cancel(id));
    }

    @PostMapping("/api/expenses/{id}/approve")
    public ApiResponse<ExpenseClaimDto> approve(@PathVariable Long id,
                                                @RequestBody(required = false) ApprovalActionRequest request) {
        return ApiResponse.ok("审批通过", expenseService.approve(id, request));
    }

    @PostMapping("/api/expenses/{id}/reject")
    public ApiResponse<ExpenseClaimDto> reject(@PathVariable Long id,
                                               @RequestBody(required = false) ApprovalActionRequest request) {
        return ApiResponse.ok("已驳回", expenseService.reject(id, request));
    }

    // ==================== 发票识别与校验 ====================

    @PostMapping("/api/expenses/invoices/recognize")
    public ApiResponse<InvoiceOcrResult> recognize(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("识别成功", ocrService.recognize(file));
    }

    @PostMapping("/api/expenses/invoices/verify")
    public ApiResponse<InvoiceVerifyDto> verify(@Valid @RequestBody InvoiceVerifyRequest request) {
        return ApiResponse.ok(expenseService.verify(request));
    }
}
