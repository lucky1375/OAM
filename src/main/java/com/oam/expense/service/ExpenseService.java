package com.oam.expense.service;

import com.oam.common.security.SecurityUtil;
import com.oam.expense.dto.*;
import com.oam.expense.entity.ExpenseClaim;
import com.oam.expense.entity.ExpenseInvoice;
import com.oam.expense.repository.ExpenseClaimRepository;
import com.oam.expense.repository.ExpenseInvoiceRepository;
import com.oam.user.entity.Role;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import com.oam.workflow.dto.ApprovalActionRequest;
import com.oam.workflow.dto.ApprovalFlowDetailDto;
import com.oam.workflow.dto.ApprovalFlowDto;
import com.oam.workflow.dto.ApprovalSubmitRequest;
import com.oam.workflow.entity.ApprovalFlow;
import com.oam.workflow.event.ApprovalFinishedEvent;
import com.oam.workflow.repository.ApprovalFlowRepository;
import com.oam.workflow.service.ApprovalService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报销业务：报销单维护、发票校验、发起审批与审批状态回写。
 */
@Service
public class ExpenseService {

    private static final List<String> CATEGORIES = Arrays.asList("TRAVEL", "OFFICE", "MEETING", "OTHER");
    private static final List<String> STATUSES = Arrays.asList("DRAFT", "PENDING", "APPROVED", "REJECTED", "CANCELLED");
    private static final List<String> INVOICE_TYPES =
            Arrays.asList("VAT_SPECIAL", "VAT_NORMAL", "VAT_ELECTRONIC", "OTHER");
    private static final List<String> SOURCES = Arrays.asList("MANUAL", "OCR");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.01");

    private final ExpenseClaimRepository claimRepository;
    private final ExpenseInvoiceRepository invoiceRepository;
    private final ApprovalFlowRepository flowRepository;
    private final UserRepository userRepository;
    private final ApprovalService approvalService;

    public ExpenseService(ExpenseClaimRepository claimRepository,
                          ExpenseInvoiceRepository invoiceRepository,
                          ApprovalFlowRepository flowRepository,
                          UserRepository userRepository,
                          ApprovalService approvalService) {
        this.claimRepository = claimRepository;
        this.invoiceRepository = invoiceRepository;
        this.flowRepository = flowRepository;
        this.userRepository = userRepository;
        this.approvalService = approvalService;
    }

    // ==================== 查询 ====================

    /** scope: mine-我提交的，todo-待我审批，all-全部（管理员） */
    @Transactional(readOnly = true)
    public List<ExpenseClaimDto> list(String scope, String keyword, String status, String category) {
        User user = requireUser();
        String sc = scope == null ? "mine" : scope.trim().toLowerCase();
        List<ExpenseClaim> claims;
        if ("todo".equals(sc)) {
            Set<Long> flowIds = approvalService.list("todo", null, "PENDING", "EXPENSE").stream()
                    .map(ApprovalFlowDto::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            claims = flowIds.isEmpty()
                    ? Collections.emptyList()
                    : claimRepository.findByFlowIdInOrderByCreatedAtDesc(flowIds);
        } else if ("all".equals(sc)) {
            if (user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("只有管理员可以查看全部报销单");
            }
            claims = claimRepository.findAllByOrderByCreatedAtDesc();
        } else {
            claims = claimRepository.findByApplicantUserIdOrderByCreatedAtDesc(user.getId());
        }
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        String st = normalize(status, STATUSES);
        String cat = normalize(category, CATEGORIES);
        Map<Long, ApprovalFlow> flowMap = flowMap(claims);
        return claims.stream()
                .filter(c -> kw.isEmpty()
                        || (c.getTitle() != null && c.getTitle().toLowerCase().contains(kw))
                        || (c.getDescription() != null && c.getDescription().toLowerCase().contains(kw)))
                .filter(c -> st == null || st.equals(c.getStatus()))
                .filter(c -> cat == null || cat.equals(c.getCategory()))
                .map(c -> toDto(c, user, flowMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseStatsDto stats() {
        User user = requireUser();
        ExpenseStatsDto dto = new ExpenseStatsDto();
        dto.setDraft(claimRepository.countByApplicantUserIdAndStatus(user.getId(), "DRAFT"));
        dto.setPending(claimRepository.countByApplicantUserIdAndStatus(user.getId(), "PENDING"));
        dto.setApproved(claimRepository.countByApplicantUserIdAndStatus(user.getId(), "APPROVED"));
        dto.setRejected(claimRepository.countByApplicantUserIdAndStatus(user.getId(), "REJECTED"));
        dto.setCancelled(claimRepository.countByApplicantUserIdAndStatus(user.getId(), "CANCELLED"));
        dto.setTodo(approvalService.list("todo", null, "PENDING", "EXPENSE").size());
        BigDecimal approvedAmount = claimRepository.sumApprovedAmount(user.getId());
        dto.setApprovedAmount(approvedAmount == null ? BigDecimal.ZERO : approvedAmount);
        return dto;
    }

    @Transactional(readOnly = true)
    public ExpenseClaimDetailDto detail(Long id) {
        User user = requireUser();
        ExpenseClaim claim = find(id);
        boolean mine = user.getId().equals(claim.getApplicantUserId());
        boolean admin = user.getRole() == Role.ADMIN;

        ApprovalFlowDetailDto flowDetail = null;
        if (claim.getFlowId() != null) {
            try {
                flowDetail = approvalService.detail(claim.getFlowId());
            } catch (IllegalArgumentException e) {
                if (!mine && !admin) {
                    throw new IllegalArgumentException("无权查看该报销单");
                }
            }
        } else if (!mine && !admin) {
            throw new IllegalArgumentException("无权查看该报销单");
        }

        ExpenseClaimDto dto = ExpenseClaimDto.from(claim, user.getId());
        if (flowDetail != null) {
            dto.setCurrentApproverName(flowDetail.getFlow().getApproverName());
            dto.setCanApprove(Boolean.TRUE.equals(flowDetail.getFlow().getCanApprove())
                    && "PENDING".equals(claim.getStatus()));
        }
        List<ExpenseInvoiceDto> invoices = invoiceRepository.findByClaimIdOrderByIdAsc(id).stream()
                .map(ExpenseInvoiceDto::from)
                .collect(Collectors.toList());
        return new ExpenseClaimDetailDto(dto, invoices,
                flowDetail == null ? null : flowDetail.getFlow(),
                flowDetail == null ? Collections.emptyList() : flowDetail.getRecords());
    }

    @Transactional(readOnly = true)
    public InvoiceVerifyDto verify(InvoiceVerifyRequest request) {
        requireUser();
        ExpenseInvoice invoice = buildInvoice(request.getClaimId(), request.getInvoice(), new HashSet<>());
        return new InvoiceVerifyDto(invoice.getVerifyStatus(), invoice.getVerifyMessage());
    }

    // ==================== 报销单维护 ====================

    @Transactional
    public ExpenseClaimDto create(ExpenseClaimRequest request) {
        User user = requireUser();
        ExpenseClaim claim = new ExpenseClaim();
        applyBasic(claim, request);
        claim.setStatus("DRAFT");
        claim.setApplicantUserId(user.getId());
        claim.setApplicantName(displayName(user));
        claim.setCreatedBy(SecurityUtil.currentUsername());
        claim = claimRepository.save(claim);
        saveInvoices(claim, request.getInvoices());
        return toDto(claim, user, flowMap(Collections.singletonList(claim)));
    }

    @Transactional
    public ExpenseClaimDto update(Long id, ExpenseClaimRequest request) {
        User user = requireUser();
        ExpenseClaim claim = find(id);
        requireOwner(claim, user, "只能编辑自己提交的报销单");
        if (!"DRAFT".equals(claim.getStatus())) {
            throw new IllegalArgumentException("只有草稿状态的报销单可以编辑");
        }
        applyBasic(claim, request);
        claim.setUpdatedBy(SecurityUtil.currentUsername());
        claim = claimRepository.save(claim);
        saveInvoices(claim, request.getInvoices());
        return toDto(claim, user, flowMap(Collections.singletonList(claim)));
    }

    @Transactional
    public void delete(Long id) {
        User user = requireUser();
        ExpenseClaim claim = find(id);
        requireOwner(claim, user, "只能删除自己提交的报销单");
        if (!Arrays.asList("DRAFT", "REJECTED", "CANCELLED").contains(claim.getStatus())) {
            throw new IllegalArgumentException("审批中或已通过的报销单不能删除");
        }
        invoiceRepository.deleteByClaimId(id);
        claimRepository.delete(claim);
    }

    // ==================== 审批 ====================

    @Transactional
    public ExpenseClaimDto submit(Long id, ExpenseSubmitRequest request) {
        User user = requireUser();
        ExpenseClaim claim = find(id);
        requireOwner(claim, user, "只能提交自己提交的报销单");
        if (!"DRAFT".equals(claim.getStatus())) {
            throw new IllegalArgumentException("只有草稿状态的报销单可以提交");
        }
        List<ExpenseInvoice> invoices = invoiceRepository.findByClaimIdOrderByIdAsc(id);
        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("请至少添加一张发票后再提交");
        }

        // 草稿期间发票可能已被其他报销单占用，提交前重新校验
        Set<String> seen = new HashSet<>();
        for (ExpenseInvoice invoice : invoices) {
            verifyInvoice(invoice, id, seen);
        }
        invoiceRepository.saveAll(invoices);
        List<ExpenseInvoice> invalid = invoices.stream()
                .filter(i -> "INVALID".equals(i.getVerifyStatus()))
                .collect(Collectors.toList());
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("发票校验未通过：" + invalid.get(0).getVerifyMessage());
        }

        BigDecimal total = invoices.stream()
                .map(ExpenseInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ApprovalSubmitRequest submit = new ApprovalSubmitRequest();
        submit.setType("EXPENSE");
        submit.setTitle(claim.getTitle());
        submit.setContent(buildContent(claim, total, invoices.size()));
        submit.setApproverUserId(request == null ? null : request.getApproverUserId());
        ApprovalFlowDto flow = approvalService.submit(submit);

        claim.setAmount(total);
        claim.setInvoiceCount(invoices.size());
        claim.setFlowId(flow.getId());
        claim.setStatus("PENDING");
        claim.setSubmittedAt(LocalDateTime.now());
        claim.setUpdatedBy(SecurityUtil.currentUsername());
        claim = claimRepository.save(claim);
        return toDto(claim, user, flowMap(Collections.singletonList(claim)));
    }

    @Transactional
    public ExpenseClaimDto cancel(Long id) {
        User user = requireUser();
        ExpenseClaim claim = find(id);
        requireOwner(claim, user, "只能撤回自己提交的报销单");
        if (!"PENDING".equals(claim.getStatus()) || claim.getFlowId() == null) {
            throw new IllegalArgumentException("当前报销单不在审批中，无法撤回");
        }
        approvalService.cancel(claim.getFlowId());
        claim.setStatus("CANCELLED");
        claim.setFinishedAt(LocalDateTime.now());
        claim.setUpdatedBy(SecurityUtil.currentUsername());
        claim = claimRepository.save(claim);
        return toDto(claim, user, flowMap(Collections.singletonList(claim)));
    }

    @Transactional
    public ExpenseClaimDto approve(Long id, ApprovalActionRequest request) {
        User user = requireUser();
        ExpenseClaim claim = find(id);
        if (claim.getFlowId() == null) {
            throw new IllegalArgumentException("该报销单尚未提交审批");
        }
        approvalService.approve(claim.getFlowId(), request);
        claim = find(id);
        return toDto(claim, user, flowMap(Collections.singletonList(claim)));
    }

    @Transactional
    public ExpenseClaimDto reject(Long id, ApprovalActionRequest request) {
        User user = requireUser();
        ExpenseClaim claim = find(id);
        if (claim.getFlowId() == null) {
            throw new IllegalArgumentException("该报销单尚未提交审批");
        }
        approvalService.reject(claim.getFlowId(), request);
        claim = find(id);
        return toDto(claim, user, flowMap(Collections.singletonList(claim)));
    }

    /** 审批流程结束后回写报销单状态 */
    @EventListener
    @Transactional
    public void onApprovalFinished(ApprovalFinishedEvent event) {
        ExpenseClaim claim = claimRepository.findByFlowId(event.getFlowId()).orElse(null);
        if (claim == null || !"PENDING".equals(claim.getStatus())) {
            return;
        }
        claim.setStatus(event.getStatus());
        claim.setFinishedAt(LocalDateTime.now());
        claim.setUpdatedBy(event.getApproverName());
        claimRepository.save(claim);
    }

    // ==================== 发票处理 ====================

    /** 覆盖保存报销单的发票明细，并重新计算报销总额与发票张数 */
    private void saveInvoices(ExpenseClaim claim, List<ExpenseInvoiceRequest> requests) {
        invoiceRepository.deleteByClaimId(claim.getId());
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        if (requests != null) {
            Set<String> seen = new HashSet<>();
            for (ExpenseInvoiceRequest request : requests) {
                ExpenseInvoice invoice = buildInvoice(claim.getId(), request, seen);
                invoice.setCreatedBy(SecurityUtil.currentUsername());
                invoiceRepository.save(invoice);
                total = total.add(invoice.getTotalAmount());
                count++;
            }
        }
        claim.setAmount(total);
        claim.setInvoiceCount(count);
        claimRepository.save(claim);
    }

    private ExpenseInvoice buildInvoice(Long claimId, ExpenseInvoiceRequest request, Set<String> seen) {
        ExpenseInvoice invoice = new ExpenseInvoice();
        invoice.setClaimId(claimId);
        String type = normalize(request.getInvoiceType(), INVOICE_TYPES);
        invoice.setInvoiceType(type == null ? "VAT_NORMAL" : type);
        invoice.setInvoiceCode(trimToNull(request.getInvoiceCode()));
        invoice.setInvoiceNumber(request.getInvoiceNumber().trim());
        invoice.setInvoiceDate(parseInvoiceDate(request.getInvoiceDate()));
        invoice.setSellerName(trimToNull(request.getSellerName()));
        invoice.setBuyerName(trimToNull(request.getBuyerName()));
        invoice.setAmount(request.getAmount());
        invoice.setTaxAmount(request.getTaxAmount());
        invoice.setTotalAmount(request.getTotalAmount());
        String source = normalize(request.getSource(), SOURCES);
        invoice.setSource(source == null ? "MANUAL" : source);
        verifyInvoice(invoice, claimId, seen);
        return invoice;
    }

    /** 基础验真：发票号查重 + 金额一致性 */
    private void verifyInvoice(ExpenseInvoice invoice, Long claimId, Set<String> seen) {
        String number = invoice.getInvoiceNumber();
        if (number == null || number.trim().isEmpty()) {
            invoice.setVerifyStatus("INVALID");
            invoice.setVerifyMessage("发票号码不能为空");
            return;
        }
        number = number.trim();
        if (seen.contains(number)) {
            invoice.setVerifyStatus("INVALID");
            invoice.setVerifyMessage("发票号码在当前报销单中重复");
            return;
        }
        seen.add(number);
        long duplicate = claimId == null
                ? invoiceRepository.countDuplicateInUse(number)
                : invoiceRepository.countDuplicate(number, claimId);
        if (duplicate > 0) {
            invoice.setVerifyStatus("INVALID");
            invoice.setVerifyMessage("该发票已存在于审批中或已通过的报销单，疑似重复报销");
            return;
        }
        BigDecimal total = invoice.getTotalAmount();
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setVerifyStatus("INVALID");
            invoice.setVerifyMessage("价税合计必须大于 0");
            return;
        }
        if (invoice.getAmount() != null && invoice.getTaxAmount() != null) {
            BigDecimal sum = invoice.getAmount().add(invoice.getTaxAmount());
            if (sum.subtract(total).abs().compareTo(AMOUNT_TOLERANCE) > 0) {
                invoice.setVerifyStatus("INVALID");
                invoice.setVerifyMessage("不含税金额与税额之和不等于价税合计");
                return;
            }
        }
        invoice.setVerifyStatus("VERIFIED");
        invoice.setVerifyMessage("基础校验通过（重复报销 / 金额一致性）");
    }

    // ==================== 辅助 ====================

    private void applyBasic(ExpenseClaim claim, ExpenseClaimRequest request) {
        claim.setTitle(request.getTitle().trim());
        String category = normalize(request.getCategory(), CATEGORIES);
        claim.setCategory(category == null ? "OTHER" : category);
        claim.setDescription(trimToNull(request.getDescription()));
    }

    private String buildContent(ExpenseClaim claim, BigDecimal total, int invoiceCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("报销金额 ¥").append(total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString())
                .append("；发票 ").append(invoiceCount).append(" 张");
        if (claim.getDescription() != null && !claim.getDescription().isEmpty()) {
            sb.append("；说明：").append(claim.getDescription());
        }
        String content = sb.toString();
        return content.length() > 2000 ? content.substring(0, 2000) : content;
    }

    private String parseInvoiceDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FMT).format(DATE_FMT);
        } catch (Exception e) {
            throw new IllegalArgumentException("开票日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    private ExpenseClaimDto toDto(ExpenseClaim claim, User user, Map<Long, ApprovalFlow> flowMap) {
        ExpenseClaimDto dto = ExpenseClaimDto.from(claim, user.getId());
        ApprovalFlow flow = claim.getFlowId() == null ? null : flowMap.get(claim.getFlowId());
        if (flow != null) {
            dto.setCurrentApproverName(flow.getApproverName());
            dto.setCanApprove("PENDING".equals(claim.getStatus())
                    && user.getId().equals(flow.getApproverUserId()));
        }
        return dto;
    }

    private Map<Long, ApprovalFlow> flowMap(List<ExpenseClaim> claims) {
        Set<Long> flowIds = claims.stream()
                .map(ExpenseClaim::getFlowId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (flowIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return flowRepository.findAllById(flowIds).stream()
                .collect(Collectors.toMap(ApprovalFlow::getId, f -> f, (a, b) -> a));
    }

    private ExpenseClaim find(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("报销单不存在"));
    }

    private void requireOwner(ExpenseClaim claim, User user, String message) {
        if (!user.getId().equals(claim.getApplicantUserId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException(message);
        }
    }

    private String normalize(String value, List<String> allowed) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String v = value.trim().toUpperCase();
        return allowed.contains(v) ? v : null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private User requireUser() {
        User user = currentUser();
        if (user == null) {
            throw new IllegalArgumentException("未登录");
        }
        return user;
    }

    private User currentUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }

    private String displayName(User user) {
        return user.getNickname() != null && !user.getNickname().isEmpty()
                ? user.getNickname() : user.getUsername();
    }
}
