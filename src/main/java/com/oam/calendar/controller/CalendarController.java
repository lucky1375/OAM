package com.oam.calendar.controller;

import com.oam.calendar.dto.CalendarCreateRequest;
import com.oam.calendar.dto.CalendarDto;
import com.oam.calendar.service.CalendarService;
import com.oam.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/calendars")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ApiResponse<List<CalendarDto>> list(@RequestParam(required = false) Long ownerUserId) {
        return ApiResponse.ok(calendarService.list(ownerUserId));
    }

    @PostMapping
    public ApiResponse<CalendarDto> create(@Valid @RequestBody CalendarCreateRequest request) {
        return ApiResponse.ok("日程创建成功", calendarService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CalendarDto> update(@PathVariable Long id,
                                           @Valid @RequestBody CalendarCreateRequest request) {
        return ApiResponse.ok("更新成功", calendarService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        calendarService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}