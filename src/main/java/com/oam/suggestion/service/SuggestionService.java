package com.oam.suggestion.service;

import com.oam.common.security.SecurityUtil;
import com.oam.suggestion.dto.SuggestionDto;
import com.oam.suggestion.dto.SuggestionReplyRequest;
import com.oam.suggestion.dto.SuggestionRequest;
import com.oam.suggestion.dto.SuggestionStatsDto;
import com.oam.suggestion.entity.Suggestion;
import com.oam.suggestion.repository.SuggestionRepository;
import com.oam.user.entity.Role;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SuggestionService {

    private static final List<String> CATEGORIES = java.util.Arrays.asList("IMPROVEMENT", "FEATURE", "BUG", "OTHER");
    private static final List<String> STATUSES = java.util.Arrays.asList("PENDING", "PROCESSING", "RESOLVED", "CLOSED");

    private final SuggestionRepository suggestionRepository;
    private final UserRepository userRepository;

    public SuggestionService(SuggestionRepository suggestionRepository, UserRepository userRepository) {
        this.suggestionRepository = suggestionRepository;
        this.userRepository = userRepository;
    }

    /** 管理员可见全部，普通用户仅可见自己提交的 */
    @Transactional(readOnly = true)
    public List<SuggestionDto> list(String keyword, String status, String category, Boolean mine) {
        User user = currentUser();
        boolean admin = isAdmin(user);
        List<Suggestion> list;
        if (admin && !Boolean.TRUE.equals(mine)) {
            list = suggestionRepository.findAllByOrderByCreatedAtDesc();
        } else {
            Long uid = user == null ? -1L : user.getId();
            list = suggestionRepository.findBySubmitterUserIdOrderByCreatedAtDesc(uid);
        }
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        String st = normalize(status, STATUSES);
        String cat = normalize(category, CATEGORIES);
        Long uid = user == null ? null : user.getId();
        return list.stream()
                .filter(s -> kw.isEmpty()
                        || (s.getTitle() != null && s.getTitle().toLowerCase().contains(kw))
                        || (s.getContent() != null && s.getContent().toLowerCase().contains(kw)))
                .filter(s -> st == null || st.equals(s.getStatus()))
                .filter(s -> cat == null || cat.equals(s.getCategory()))
                .map(s -> SuggestionDto.from(s, uid))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SuggestionDto detail(Long id) {
        Suggestion s = find(id);
        Long uid = currentUserId();
        if (!isAdmin(currentUser()) && !s.getSubmitterUserId().equals(uid)) {
            throw new IllegalArgumentException("无权查看该建议");
        }
        return SuggestionDto.from(s, uid);
    }

    @Transactional
    public SuggestionDto create(SuggestionRequest request) {
        User user = currentUser();
        if (user == null) {
            throw new IllegalArgumentException("未登录");
        }
        Suggestion s = new Suggestion();
        s.setTitle(request.getTitle().trim());
        s.setContent(request.getContent().trim());
        s.setCategory(normalize(request.getCategory(), CATEGORIES) == null ? "IMPROVEMENT"
                : normalize(request.getCategory(), CATEGORIES));
        s.setStatus("PENDING");
        s.setSubmitterUserId(user.getId());
        s.setSubmitterName(displayName(user));
        s.setAnonymous(Boolean.TRUE.equals(request.getAnonymous()));
        s.setCreatedBy(SecurityUtil.currentUsername());
        s = suggestionRepository.save(s);
        return SuggestionDto.from(s, user.getId());
    }

    @Transactional
    public SuggestionDto update(Long id, SuggestionRequest request) {
        Suggestion s = find(id);
        User user = currentUser();
        if (!s.getSubmitterUserId().equals(currentUserId())) {
            throw new IllegalArgumentException("只能编辑自己提交的建议");
        }
        if (!"PENDING".equals(s.getStatus())) {
            throw new IllegalArgumentException("该建议已进入处理流程，不能再编辑");
        }
        s.setTitle(request.getTitle().trim());
        s.setContent(request.getContent().trim());
        String cat = normalize(request.getCategory(), CATEGORIES);
        if (cat != null) s.setCategory(cat);
        if (request.getAnonymous() != null) s.setAnonymous(request.getAnonymous());
        s.setUpdatedBy(SecurityUtil.currentUsername());
        s = suggestionRepository.save(s);
        return SuggestionDto.from(s, user == null ? null : user.getId());
    }

    /** 管理员回复并更新处理状态 */
    @Transactional
    public SuggestionDto reply(Long id, SuggestionReplyRequest request) {
        Suggestion s = find(id);
        User user = currentUser();
        if (!isAdmin(user)) {
            throw new IllegalArgumentException("只有管理员可以回复建议");
        }
        s.setReplyContent(request.getReplyContent().trim());
        s.setReplierName(user == null ? SecurityUtil.currentUsername() : displayName(user));
        s.setRepliedAt(LocalDateTime.now());
        String st = normalize(request.getStatus(), STATUSES);
        s.setStatus(st == null ? "RESOLVED" : st);
        s.setUpdatedBy(SecurityUtil.currentUsername());
        s = suggestionRepository.save(s);
        return SuggestionDto.from(s, currentUserId());
    }

    @Transactional
    public void delete(Long id) {
        Suggestion s = find(id);
        User user = currentUser();
        boolean admin = isAdmin(user);
        if (!admin && !s.getSubmitterUserId().equals(currentUserId())) {
            throw new IllegalArgumentException("无权删除该建议");
        }
        suggestionRepository.delete(s);
    }

    /** 统计当前用户可见范围内的数据 */
    @Transactional(readOnly = true)
    public SuggestionStatsDto stats() {
        User user = currentUser();
        List<Suggestion> list = isAdmin(user)
                ? suggestionRepository.findAll()
                : suggestionRepository.findBySubmitterUserIdOrderByCreatedAtDesc(
                        user == null ? -1L : user.getId());
        SuggestionStatsDto dto = new SuggestionStatsDto();
        dto.setTotal(list.size());
        dto.setPending(list.stream().filter(s -> "PENDING".equals(s.getStatus())).count());
        dto.setProcessing(list.stream().filter(s -> "PROCESSING".equals(s.getStatus())).count());
        dto.setResolved(list.stream().filter(s -> "RESOLVED".equals(s.getStatus())).count());
        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (String c : CATEGORIES) {
            byCategory.put(c, list.stream().filter(s -> c.equals(s.getCategory())).count());
        }
        dto.setByCategory(byCategory);
        return dto;
    }

    // ==================== 辅助 ====================

    private Suggestion find(Long id) {
        return suggestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("建议不存在"));
    }

    private String normalize(String value, List<String> allowed) {
        if (value == null || value.trim().isEmpty()) return null;
        String v = value.trim().toUpperCase();
        return allowed.contains(v) ? v : null;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    private User currentUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    private Long currentUserId() {
        User u = currentUser();
        return u == null ? null : u.getId();
    }

    private String displayName(User u) {
        return u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername();
    }
}
