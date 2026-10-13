package com.oam.position.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.position.dto.PositionCreateRequest;
import com.oam.position.dto.PositionDto;
import com.oam.position.dto.PositionUpdateRequest;
import com.oam.position.service.PositionService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public ApiResponse<List<PositionDto>> list(@RequestParam(required = false) Long departmentId,
                                               @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(positionService.list(departmentId, keyword));
    }

    @PostMapping
    public ApiResponse<PositionDto> create(@Valid @RequestBody PositionCreateRequest request) {
        return ApiResponse.ok("创建成功", positionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PositionDto> update(@PathVariable Long id,
                                           @Valid @RequestBody PositionUpdateRequest request) {
        return ApiResponse.ok("更新成功", positionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
