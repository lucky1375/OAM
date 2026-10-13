package com.oam.meeting.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.meeting.dto.MeetingCreateRequest;
import com.oam.meeting.dto.MeetingDto;
import com.oam.meeting.dto.MeetingRoomCreateRequest;
import com.oam.meeting.dto.MeetingRoomDto;
import com.oam.meeting.service.MeetingService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    // ==================== 会议室管理 ====================

    @GetMapping("/api/meeting-rooms")
    public ApiResponse<List<MeetingRoomDto>> listRooms(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(meetingService.listRooms(keyword));
    }

    @PostMapping("/api/meeting-rooms")
    public ApiResponse<MeetingRoomDto> createRoom(@Valid @RequestBody MeetingRoomCreateRequest request) {
        return ApiResponse.ok("会议室创建成功", meetingService.createRoom(request));
    }

    @PutMapping("/api/meeting-rooms/{id}")
    public ApiResponse<MeetingRoomDto> updateRoom(@PathVariable Long id,
                                                  @Valid @RequestBody MeetingRoomCreateRequest request) {
        return ApiResponse.ok("更新成功", meetingService.updateRoom(id, request));
    }

    @DeleteMapping("/api/meeting-rooms/{id}")
    public ApiResponse<Void> deleteRoom(@PathVariable Long id) {
        meetingService.deleteRoom(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ==================== 会议管理 ====================

    @GetMapping("/api/meetings")
    public ApiResponse<List<MeetingDto>> listMeetings(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(meetingService.listMeetings(keyword));
    }

    @PostMapping("/api/meetings")
    public ApiResponse<MeetingDto> createMeeting(@Valid @RequestBody MeetingCreateRequest request) {
        return ApiResponse.ok("会议创建成功", meetingService.createMeeting(request));
    }

    @PutMapping("/api/meetings/{id}")
    public ApiResponse<MeetingDto> updateMeeting(@PathVariable Long id,
                                                 @Valid @RequestBody MeetingCreateRequest request) {
        return ApiResponse.ok("更新成功", meetingService.updateMeeting(id, request));
    }

    @DeleteMapping("/api/meetings/{id}")
    public ApiResponse<Void> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ApiResponse.ok("删除成功", null);
    }
}