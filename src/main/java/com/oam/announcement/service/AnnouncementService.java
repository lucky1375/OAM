package com.oam.announcement.service;

import com.oam.announcement.dto.*;
import com.oam.announcement.entity.Announcement;
import com.oam.announcement.entity.AnnouncementRead;
import com.oam.announcement.repository.AnnouncementReadRepository;
import com.oam.announcement.repository.AnnouncementRepository;
import com.oam.common.security.SecurityUtil;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository readRepository;
    private final UserRepository userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               AnnouncementReadRepository readRepository,
                               UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.readRepository = readRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> list(String keyword, String status) {
        String kw = keyword == null ? null : keyword.trim();
        String st = normalizeStatus(status, false);
        List<Announcement> list;
        if (st != null && kw != null && !kw.isEmpty()) {
            list = announcementRepository.findByStatusAndTitleContainingOrderByTopDescPublishTimeDesc(st, kw);
        } else if (st != null) {
            list = announcementRepository.findByStatusOrderByTopDescPublishTimeDesc(st);
        } else if (kw != null && !kw.isEmpty()) {
            list = announcementRepository.findByTitleContainingOrderByTopDescPublishTimeDesc(kw);
        } else {
            list = announcementRepository.findAll();
            list.sort((a, b) -> {
                int top = Boolean.compare(Boolean.TRUE.equals(b.getTop()), Boolean.TRUE.equals(a.getTop()));
                if (top != 0) return top;
                LocalDateTime ta = a.getPublishTime() != null ? a.getPublishTime() : a.getCreatedAt();
                LocalDateTime tb = b.getPublishTime() != null ? b.getPublishTime() : b.getCreatedAt();
                return tb.compareTo(ta);
            });
        }
        Long userId = currentUserId();
        return list.stream()
                .map(a -> AnnouncementDto.from(a, userId != null && readRepository.existsByAnnouncementIdAndUserId(a.getId(), userId)))
                .collect(Collectors.toList());
    }

    /** 查看详情，并自动标记当前用户已读 */
    @Transactional
    public AnnouncementDetailDto getDetail(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        boolean readByMe = markReadInternal(a);
        return AnnouncementDetailDto.from(a, readByMe);
    }

    @Transactional
    public AnnouncementDto create(AnnouncementRequest request) {
        User user = currentUser();
        Announcement a = new Announcement();
        a.setTitle(request.getTitle().trim());
        a.setContent(request.getContent());
        a.setPublisherUserId(user != null ? user.getId() : null);
        a.setPublisherName(user != null ? displayName(user) : SecurityUtil.currentUsername());
        a.setTop(Boolean.TRUE.equals(request.getTop()));
        a.setReadCount(0);
        a.setCreatedBy(SecurityUtil.currentUsername());
        applyStatus(a, request.getStatus());
        a = announcementRepository.save(a);
        return AnnouncementDto.from(a, false);
    }

    @Transactional
    public AnnouncementDto update(Long id, AnnouncementRequest request) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            a.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            a.setContent(request.getContent());
        }
        if (request.getTop() != null) {
            a.setTop(request.getTop());
        }
        if (request.getStatus() != null) {
            applyStatus(a, request.getStatus());
        }
        a.setUpdatedBy(SecurityUtil.currentUsername());
        a = announcementRepository.save(a);
        Long userId = currentUserId();
        boolean readByMe = userId != null && readRepository.existsByAnnouncementIdAndUserId(id, userId);
        return AnnouncementDto.from(a, readByMe);
    }

    @Transactional
    public void delete(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new IllegalArgumentException("公告不存在");
        }
        readRepository.deleteByAnnouncementId(id);
        announcementRepository.deleteById(id);
    }

    /** 标记当前用户已读，返回最新已读人数 */
    @Transactional
    public int markRead(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        markReadInternal(a);
        return a.getReadCount();
    }

    @Transactional(readOnly = true)
    public List<ReadStatDto> readStats(Long id) {
        announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        return readRepository.findByAnnouncementIdOrderByCreatedAtDesc(id).stream()
                .map(ReadStatDto::from)
                .collect(Collectors.toList());
    }

    // ==================== 辅助 ====================

    /** 记录阅读并同步冗余计数，返回当前用户是否已读 */
    private boolean markReadInternal(Announcement a) {
        Long userId = currentUserId();
        if (userId == null) {
            return false;
        }
        if (!readRepository.existsByAnnouncementIdAndUserId(a.getId(), userId)) {
            AnnouncementRead read = new AnnouncementRead();
            read.setAnnouncementId(a.getId());
            read.setUserId(userId);
            User u = currentUser();
            read.setUserName(u != null ? displayName(u) : null);
            readRepository.save(read);
            a.setReadCount((int) readRepository.countByAnnouncementId(a.getId()));
            announcementRepository.save(a);
        }
        return true;
    }

    /** 应用状态：PUBLISHED 时补发布时间，DRAFT 时清空发布时间 */
    private void applyStatus(Announcement a, String status) {
        String st = normalizeStatus(status, true);
        if (st == null) {
            st = a.getStatus() == null ? "PUBLISHED" : a.getStatus();
        }
        a.setStatus(st);
        if ("PUBLISHED".equals(st)) {
            if (a.getPublishTime() == null) {
                a.setPublishTime(LocalDateTime.now());
            }
        } else {
            a.setPublishTime(null);
        }
    }

    private String normalizeStatus(String status, boolean defaultPublished) {
        if (status == null || status.trim().isEmpty()) {
            return defaultPublished ? "PUBLISHED" : null;
        }
        String st = status.trim().toUpperCase();
        if ("DRAFT".equals(st) || "PUBLISHED".equals(st)) {
            return st;
        }
        return defaultPublished ? "PUBLISHED" : null;
    }

    // ==================== 辅助 ====================

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
