package com.oam.suggestion.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.suggestion.dto.SuggestionDto;
import com.oam.suggestion.dto.SuggestionReplyRequest;
import com.oam.suggestion.dto.SuggestionRequest;
import com.oam.suggestion.dto.SuggestionStatsDto;
import com.oam.suggestion.service.SuggestionService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping("/api/suggestions")
    public ApiResponse<List<SuggestionDto>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String category,
                                                 @RequestParam(required = false) Boolean mine) {
        return ApiResponse.ok(suggestionService.list(keyword, status, category, mine));
    }

    @GetMapping("/api/suggestions/stats")
    public ApiResponse<SuggestionStatsDto> stats() {
        return ApiResponse.ok(suggestionService.stats());
    }

    @GetMapping("/api/suggestions/{id}")
    public ApiResponse<SuggestionDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(suggestionService.detail(id));
    }

    @PostMapping("/api/suggestions")
    public ApiResponse<SuggestionDto> create(@Valid @RequestBody SuggestionRequest request) {
        return ApiResponse.ok("提交成功，我们会尽快处理", suggestionService.create(request));
    }

    @PutMapping("/api/suggestions/{id}")
    public ApiResponse<SuggestionDto> update(@PathVariable Long id,
                                             @Valid @RequestBody SuggestionRequest request) {
        return ApiResponse.ok("更新成功", suggestionService.update(id, request));
    }

    @PostMapping("/api/suggestions/{id}/reply")
    public ApiResponse<SuggestionDto> reply(@PathVariable Long id,
                                            @Valid @RequestBody SuggestionReplyRequest request) {
        return ApiResponse.ok("回复成功", suggestionService.reply(id, request));
    }

    @DeleteMapping("/api/suggestions/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        suggestionService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
