package com.oam.announcement.controller;

import com.oam.announcement.dto.AnnouncementDetailDto;
import com.oam.announcement.dto.AnnouncementDto;
import com.oam.announcement.dto.AnnouncementRequest;
import com.oam.announcement.dto.ReadStatDto;
import com.oam.announcement.service.AnnouncementService;
import com.oam.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/api/announcements")
    public ApiResponse<List<AnnouncementDto>> list(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String status) {
        return ApiResponse.ok(announcementService.list(keyword, status));
    }

    @GetMapping("/api/announcements/{id}")
    public ApiResponse<AnnouncementDetailDto> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(announcementService.getDetail(id));
    }

    @PostMapping("/api/announcements")
    public ApiResponse<AnnouncementDto> create(@Valid @RequestBody AnnouncementRequest request) {
        boolean draft = request.getStatus() != null && "DRAFT".equalsIgnoreCase(request.getStatus().trim());
        return ApiResponse.ok(draft ? "已存为草稿" : "公告发布成功", announcementService.create(request));
    }

    @PutMapping("/api/announcements/{id}")
    public ApiResponse<AnnouncementDto> update(@PathVariable Long id,
                                               @Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.ok("更新成功", announcementService.update(id, request));
    }

    @DeleteMapping("/api/announcements/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    /** 标记已读，返回最新已读人数 */
    @PostMapping("/api/announcements/{id}/read")
    public ApiResponse<Integer> markRead(@PathVariable Long id) {
        return ApiResponse.ok(announcementService.markRead(id));
    }

    /** 阅读统计：已读人员列表 */
    @GetMapping("/api/announcements/{id}/reads")
    public ApiResponse<List<ReadStatDto>> readStats(@PathVariable Long id) {
        return ApiResponse.ok(announcementService.readStats(id));
    }
}
