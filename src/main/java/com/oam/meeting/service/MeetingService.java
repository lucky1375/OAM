package com.oam.meeting.service;

import com.oam.common.security.SecurityUtil;
import com.oam.calendar.entity.CalendarEvent;
import com.oam.calendar.repository.CalendarRepository;
import com.oam.meeting.dto.MeetingCreateRequest;
import com.oam.meeting.dto.MeetingDto;
import com.oam.meeting.dto.MeetingRoomCreateRequest;
import com.oam.meeting.dto.MeetingRoomDto;
import com.oam.meeting.entity.Meeting;
import com.oam.meeting.entity.MeetingRoom;
import com.oam.meeting.repository.MeetingRepository;
import com.oam.meeting.repository.MeetingRoomRepository;
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
public class MeetingService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MeetingRoomRepository meetingRoomRepository;
    private final MeetingRepository meetingRepository;
    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    public MeetingService(MeetingRoomRepository meetingRoomRepository, MeetingRepository meetingRepository,
                          CalendarRepository calendarRepository, UserRepository userRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
        this.meetingRepository = meetingRepository;
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;
    }

    // ==================== 会议室管理 ====================

    public List<MeetingRoomDto> listRooms(String keyword) {
        List<MeetingRoom> list = meetingRoomRepository.findAll();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        if (!kw.isEmpty()) {
            list = list.stream().filter(r ->
                    (r.getName() != null && r.getName().toLowerCase().contains(kw))
                            || (r.getLocation() != null && r.getLocation().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        }
        return list.stream().map(MeetingRoomDto::from).collect(Collectors.toList());
    }

    @Transactional
    public MeetingRoomDto createRoom(MeetingRoomCreateRequest request) {
        if (meetingRoomRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("会议室名称已存在");
        }
        MeetingRoom r = new MeetingRoom();
        applyRoom(r, request);
        r.setCreatedBy(SecurityUtil.currentUsername());
        r = meetingRoomRepository.save(r);
        return MeetingRoomDto.from(r);
    }

    @Transactional
    public MeetingRoomDto updateRoom(Long id, MeetingRoomCreateRequest request) {
        MeetingRoom r = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会议室不存在"));
        if (meetingRoomRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("会议室名称已存在");
        }
        applyRoom(r, request);
        r.setUpdatedBy(SecurityUtil.currentUsername());
        r.setUpdatedAt(LocalDateTime.now());
        r = meetingRoomRepository.save(r);
        return MeetingRoomDto.from(r);
    }

    @Transactional
    public void deleteRoom(Long id) {
        MeetingRoom r = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会议室不存在"));
        long usedCount = meetingRepository.findByMeetingRoomId(id).size();
        if (usedCount > 0) {
            throw new IllegalArgumentException("该会议室已被会议占用，不能删除");
        }
        meetingRoomRepository.delete(r);
    }

    private void applyRoom(MeetingRoom r, MeetingRoomCreateRequest req) {
        r.setName(req.getName());
        r.setLocation(req.getLocation());
        r.setCapacity(req.getCapacity());
        r.setEquipment(req.getEquipment());
        r.setEnabled(req.getEnabled() == null ? true : req.getEnabled());
        r.setRemark(req.getRemark());
    }

    // ==================== 会议管理 ====================

    public List<MeetingDto> listMeetings(String keyword) {
        List<Meeting> list = meetingRepository.findAll();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        if (!kw.isEmpty()) {
            list = list.stream().filter(m ->
                    (m.getTitle() != null && m.getTitle().toLowerCase().contains(kw))
                            || (m.getOrganizer() != null && m.getOrganizer().toLowerCase().contains(kw))
                            || (m.getParticipants() != null && m.getParticipants().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        }
        Map<Long, String> roomCache = roomNameCache();
        return list.stream()
                .map(m -> MeetingDto.from(m, roomCache.get(m.getMeetingRoomId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public MeetingDto createMeeting(MeetingCreateRequest request) {
        validateTime(request);
        LocalDateTime start = parse(request.getStartTime());
        LocalDateTime end = parse(request.getEndTime());
        checkConflict(request.getMeetingRoomId(), start, end, null);

        Meeting m = new Meeting();
        applyMeeting(m, request);
        m.setCreatedBy(SecurityUtil.currentUsername());
        m = meetingRepository.save(m);
        syncToCalendar(m);
        return MeetingDto.from(m, roomName(m.getMeetingRoomId()));
    }

    @Transactional
    public MeetingDto updateMeeting(Long id, MeetingCreateRequest request) {
        Meeting m = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会议不存在"));
        validateTime(request);
        LocalDateTime start = parse(request.getStartTime());
        LocalDateTime end = parse(request.getEndTime());
        checkConflict(request.getMeetingRoomId(), start, end, id);
        applyMeeting(m, request);
        m.setUpdatedBy(SecurityUtil.currentUsername());
        m.setUpdatedAt(LocalDateTime.now());
        m = meetingRepository.save(m);
        syncToCalendar(m);
        return MeetingDto.from(m, roomName(m.getMeetingRoomId()));
    }

    @Transactional
    public void deleteMeeting(Long id) {
        Meeting m = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会议不存在"));
        calendarRepository.findBySourceMeetingId(id).forEach(calendarRepository::delete);
        meetingRepository.delete(m);
    }

    // ==================== 会议 → 日程 自动同步 ====================

    private Long currentUserId() {
        String username = SecurityUtil.currentUsername();
        if (username == null) return null;
        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }

    /** 会议保存后同步/更新到所有人的日程：取消则删除；否则新建或覆盖关联日程 */
    private void syncToCalendar(Meeting m) {
        CalendarEvent ev = calendarRepository.findBySourceMeetingId(m.getId()).stream().findFirst().orElse(null);
        if ("CANCELLED".equals(m.getStatus())) {
            if (ev != null) calendarRepository.delete(ev);
            return;
        }
        boolean isNew = ev == null;
        if (isNew) {
            ev = new CalendarEvent();
            ev.setSourceMeetingId(m.getId());
            ev.setOwnerUserId(currentUserId());
            ev.setCreatedBy(SecurityUtil.currentUsername());
        }
        String room = roomName(m.getMeetingRoomId());
        ev.setTitle(m.getTitle() != null ? "[会议] " + m.getTitle() : "[会议]");
        ev.setStartTime(m.getStartTime());
        ev.setEndTime(m.getEndTime());
        ev.setAllDay(false);
        ev.setLocation(room);
        ev.setRemark((m.getOrganizer() != null ? "发起人：" + m.getOrganizer() : "") +
                (m.getParticipants() != null && !m.getParticipants().isEmpty() ? "，参会人：" + m.getParticipants() : ""));
        if (!isNew) {
            ev.setUpdatedBy(SecurityUtil.currentUsername());
            ev.setUpdatedAt(LocalDateTime.now());
        }
        calendarRepository.save(ev);
    }

    /** 会议室时间冲突检测：同一会议室时间区间重叠即视为冲突 */
    private void checkConflict(Long roomId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (roomId == null || start == null || end == null) return;
        List<Meeting> overlaps = meetingRepository
                .findByMeetingRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(roomId, end, start);
        for (Meeting m : overlaps) {
            if (!m.getId().equals(excludeId)) {
                throw new IllegalArgumentException("该会议室在该时段已被其他会议占用");
            }
        }
    }

    private void applyMeeting(Meeting m, MeetingCreateRequest req) {
        m.setTitle(req.getTitle());
        m.setMeetingRoomId(req.getMeetingRoomId());
        m.setStartTime(parse(req.getStartTime()));
        m.setEndTime(parse(req.getEndTime()));
        m.setOrganizer(req.getOrganizer());
        m.setParticipants(req.getParticipants());
        m.setAgenda(req.getAgenda());
        m.setStatus(req.getStatus() == null ? "SCHEDULED" : req.getStatus());
        m.setRemark(req.getRemark());
    }

    private void validateTime(MeetingCreateRequest req) {
        LocalDateTime start = parse(req.getStartTime());
        LocalDateTime end = parse(req.getEndTime());
        if (start == null || end == null) {
            throw new IllegalArgumentException("请选择会议的起止时间");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
    }

    private LocalDateTime parse(String s) {
        if (s == null || s.isEmpty()) return null;
        return LocalDateTime.parse(s, FMT);
    }

    private Map<Long, String> roomNameCache() {
        Map<Long, String> map = new HashMap<>();
        for (MeetingRoom r : meetingRoomRepository.findAll()) map.put(r.getId(), r.getName());
        return map;
    }

    private String roomName(Long id) {
        if (id == null) return null;
        return meetingRoomRepository.findById(id).map(MeetingRoom::getName).orElse(null);
    }
}