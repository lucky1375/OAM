package com.oam.calendar.service;

import com.oam.calendar.dto.CalendarCreateRequest;
import com.oam.calendar.dto.CalendarDto;
import com.oam.calendar.entity.CalendarEvent;
import com.oam.calendar.repository.CalendarRepository;
import com.oam.common.security.SecurityUtil;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    protected static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    public CalendarService(CalendarRepository calendarRepository, UserRepository userRepository) {
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;
    }

    /** 当前登录用户的 User.id（未登录返回 null） */
    private Long currentUserId() {
        String username = SecurityUtil.currentUsername();
        if (username == null) return null;
        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }

    public List<CalendarDto> list(Long ownerUserId) {
        // 未指定 owner 时默认展示当前登录人员自己的日程
        Long owner = ownerUserId != null ? ownerUserId : currentUserId();
        List<CalendarEvent> list = owner != null
                ? calendarRepository.findByOwnerUserId(owner)
                : calendarRepository.findAll();
        Map<Long, String> nameCache = ownerNameCache();
        return list.stream()
                .map(e -> CalendarDto.from(e, nameCache.get(e.getOwnerUserId())))
                .collect(Collectors.toList());
    }

    private Map<Long, String> ownerNameCache() {
        Map<Long, String> map = new HashMap<>();
        for (User u : userRepository.findAll()) {
            map.put(u.getId(), u.getNickname() != null ? u.getNickname() : u.getUsername());
        }
        return map;
    }

    @Transactional
    public CalendarDto create(CalendarCreateRequest request) {
        validate(request);
        CalendarEvent e = new CalendarEvent();
        apply(e, request);
        e.setOwnerUserId(request.getOwnerUserId() != null ? request.getOwnerUserId() : currentUserId());
        e.setCreatedBy(SecurityUtil.currentUsername());
        e = calendarRepository.save(e);
        return CalendarDto.from(e, null);
    }

    @Transactional
    public CalendarDto update(Long id, CalendarCreateRequest request) {
        validate(request);
        CalendarEvent e = calendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("日程不存在"));
        apply(e, request);
        if (request.getOwnerUserId() != null) e.setOwnerUserId(request.getOwnerUserId());
        e.setUpdatedBy(SecurityUtil.currentUsername());
        e.setUpdatedAt(LocalDateTime.now());
        e = calendarRepository.save(e);
        return CalendarDto.from(e, null);
    }

    @Transactional
    public void delete(Long id) {
        CalendarEvent e = calendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("日程不存在"));
        calendarRepository.delete(e);
    }

    private void apply(CalendarEvent e, CalendarCreateRequest r) {
        e.setTitle(r.getTitle());
        e.setStartTime((r.getStartTime() == null || r.getStartTime().isEmpty()) ? null : LocalDateTime.parse(r.getStartTime(), FMT));
        e.setEndTime((r.getEndTime() == null || r.getEndTime().isEmpty()) ? null : LocalDateTime.parse(r.getEndTime(), FMT));
        e.setAllDay(r.getAllDay() == null ? false : r.getAllDay());
        e.setLocation(r.getLocation());
        e.setRemindMinutes(r.getRemindMinutes());
        e.setRemark(r.getRemark());
    }

    private void validate(CalendarCreateRequest r) {
        if (r.getStartTime() != null && !r.getStartTime().isEmpty()) {
            LocalDateTime.parse(r.getStartTime(), FMT);
        }
        if (r.getEndTime() != null && !r.getEndTime().isEmpty()) {
            LocalDateTime.parse(r.getEndTime(), FMT);
        }
    }
}