package com.oam.meeting.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.meeting.dto.AccountOptionDto;
import com.oam.meeting.dto.MeetingCreateRequest;
import com.oam.meeting.dto.MeetingDto;
import com.oam.meeting.dto.MeetingRoomCreateRequest;
import com.oam.meeting.dto.MeetingRoomDto;
import com.oam.meeting.dto.UserGroupDto;
import com.oam.meeting.dto.UserGroupRequest;
import com.oam.meeting.service.MeetingService;
import com.oam.meeting.service.UserGroupService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class MeetingController {

    private final MeetingService meetingService;
    private final UserGroupService userGroupService;

    public MeetingController(MeetingService meetingService, UserGroupService userGroupService) {
        this.meetingService = meetingService;
        this.userGroupService = userGroupService;
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

    // ==================== 参会人账号 / 个人分组 ====================

    /** 账号下拉：任何登录用户均可访问，仅返回必要字段 */
    @GetMapping("/api/meeting/accounts")
    public ApiResponse<List<AccountOptionDto>> listAccounts(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(meetingService.listAccounts(keyword));
    }

    @GetMapping("/api/meeting/groups")
    public ApiResponse<List<UserGroupDto>> listGroups() {
        return ApiResponse.ok(userGroupService.listGroups());
    }

    @PostMapping("/api/meeting/groups")
    public ApiResponse<UserGroupDto> createGroup(@Valid @RequestBody UserGroupRequest request) {
        return ApiResponse.ok("分组已创建", userGroupService.create(request));
    }

    @PutMapping("/api/meeting/groups/{id}")
    public ApiResponse<UserGroupDto> updateGroup(@PathVariable Long id,
                                                 @Valid @RequestBody UserGroupRequest request) {
        return ApiResponse.ok("分组已更新", userGroupService.update(id, request));
    }

    @DeleteMapping("/api/meeting/groups/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        userGroupService.delete(id);
        return ApiResponse.ok("分组已删除", null);
    }
}