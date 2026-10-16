package com.oam.meeting.service;

import com.oam.common.security.SecurityUtil;
import com.oam.calendar.entity.CalendarEvent;
import com.oam.calendar.repository.CalendarRepository;
import com.oam.meeting.dto.AccountOptionDto;
import com.oam.meeting.dto.MeetingCreateRequest;
import com.oam.meeting.dto.MeetingDto;
import com.oam.meeting.dto.MeetingRoomCreateRequest;
import com.oam.meeting.dto.MeetingRoomDto;
import com.oam.meeting.entity.Meeting;
import com.oam.meeting.entity.MeetingParticipant;
import com.oam.meeting.entity.MeetingRoom;
import com.oam.meeting.repository.MeetingParticipantRepository;
import com.oam.meeting.repository.MeetingRepository;
import com.oam.meeting.repository.MeetingRoomRepository;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MeetingRoomRepository meetingRoomRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    public MeetingService(MeetingRoomRepository meetingRoomRepository, MeetingRepository meetingRepository,
                          MeetingParticipantRepository participantRepository,
                          CalendarRepository calendarRepository, UserRepository userRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
        this.meetingRepository = meetingRepository;
        this.participantRepository = participantRepository;
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
        Map<Long, List<Long>> participantCache = participantCache();
        return list.stream()
                .map(m -> MeetingDto.from(m, roomCache.get(m.getMeetingRoomId()),
                        participantCache.getOrDefault(m.getId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    /** 账号下拉（仅登录即可访问，只返回必要字段供选择参会人） */
    public List<AccountOptionDto> listAccounts(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        return userRepository.findAllByOrderByCreatedAtAsc().stream()
                .filter(u -> u.getEnabled() == null || u.getEnabled())
                .filter(u -> kw.isEmpty()
                        || (u.getNickname() != null && u.getNickname().toLowerCase().contains(kw))
                        || (u.getUsername() != null && u.getUsername().toLowerCase().contains(kw)))
                .map(AccountOptionDto::from)
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
        // 发起人默认取当前登录账号
        if (m.getOrganizer() == null || m.getOrganizer().trim().isEmpty()) {
            m.setOrganizer(currentDisplayName());
        }
        // 参会人选择已有账号时，同步姓名文本与关联记录
        m.setParticipants(resolveParticipantText(request));
        m.setCreatedBy(SecurityUtil.currentUsername());
        m = meetingRepository.save(m);
        saveParticipants(m.getId(), request.getParticipantIds());
        syncToCalendar(m);
        return MeetingDto.from(m, roomName(m.getMeetingRoomId()), participantIdsOf(m.getId()));
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
        if (m.getOrganizer() == null || m.getOrganizer().trim().isEmpty()) {
            m.setOrganizer(currentDisplayName());
        }
        // participantIds 未传时保留原有参会人文本
        if (request.getParticipantIds() != null || request.getParticipants() != null) {
            m.setParticipants(resolveParticipantText(request));
        }
        m.setUpdatedBy(SecurityUtil.currentUsername());
        m.setUpdatedAt(LocalDateTime.now());
        m = meetingRepository.save(m);
        if (request.getParticipantIds() != null) {
            saveParticipants(m.getId(), request.getParticipantIds());
        }
        syncToCalendar(m);
        return MeetingDto.from(m, roomName(m.getMeetingRoomId()), participantIdsOf(m.getId()));
    }

    @Transactional
    public void deleteMeeting(Long id) {
        Meeting m = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会议不存在"));
        participantRepository.deleteByMeetingId(id);
        calendarRepository.findBySourceMeetingId(id).forEach(calendarRepository::delete);
        meetingRepository.delete(m);
    }

    // ==================== 会议 → 日程 自动同步 ====================

    private Long currentUserId() {
        User u = currentUser();
        return u == null ? null : u.getId();
    }

    private User currentUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) return null;
        return userRepository.findByUsername(username).orElse(null);
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
        m.setAgenda(req.getAgenda());
        m.setStatus(req.getStatus() == null ? "SCHEDULED" : req.getStatus());
        m.setRemark(req.getRemark());
    }

    // ==================== 参会人（账号选择） ====================

    /** 由所选账号ID解析出参会人姓名文本（未选择账号时回退文本字段） */
    private String resolveParticipantText(MeetingCreateRequest req) {
        List<Long> ids = req.getParticipantIds();
        if (ids == null) {
            return req.getParticipants();
        }
        List<String> names = namesOf(ids);
        return names.isEmpty() ? null : String.join(",", names);
    }

    /** 覆盖式保存参会人记录（传空列表表示清空） */
    private void saveParticipants(Long meetingId, List<Long> ids) {
        if (ids == null) {
            return;
        }
        participantRepository.deleteByMeetingId(meetingId);
        // 先落库删除，避免与随后插入的同名(meetingId,userId)记录冲突
        participantRepository.flush();
        Set<Long> unique = new LinkedHashSet<>(ids);
        for (Long uid : unique) {
            if (uid == null) continue;
            MeetingParticipant p = new MeetingParticipant();
            p.setMeetingId(meetingId);
            p.setUserId(uid);
            participantRepository.save(p);
        }
    }

    private List<Long> participantIdsOf(Long meetingId) {
        return participantRepository.findByMeetingId(meetingId).stream()
                .map(MeetingParticipant::getUserId)
                .collect(Collectors.toList());
    }

    private Map<Long, List<Long>> participantCache() {
        Map<Long, List<Long>> map = new HashMap<>();
        for (MeetingParticipant p : participantRepository.findAll()) {
            map.computeIfAbsent(p.getMeetingId(), k -> new ArrayList<>()).add(p.getUserId());
        }
        return map;
    }

    /** 账号ID → 显示名（找不到的账号回退为“账号{id}”） */
    private List<String> namesOf(List<Long> userIds) {
        List<String> names = new ArrayList<>();
        for (Long uid : new LinkedHashSet<>(userIds)) {
            if (uid == null) continue;
            User u = userRepository.findById(uid).orElse(null);
            names.add(u != null ? displayName(u) : ("账号" + uid));
        }
        return names;
    }

    private String currentDisplayName() {
        User u = currentUser();
        return u != null ? displayName(u) : SecurityUtil.currentUsername();
    }

    private String displayName(User u) {
        return u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername();
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