package com.oam.department.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.department.dto.DepartmentCreateRequest;
import com.oam.department.dto.DepartmentDto;
import com.oam.department.dto.DepartmentUpdateRequest;
import com.oam.department.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<DepartmentDto>> tree() {
        return ApiResponse.ok(departmentService.tree());
    }

    @GetMapping("/flat")
    public ApiResponse<List<DepartmentDto>> flat() {
        return ApiResponse.ok(departmentService.flatList());
    }

    @PostMapping
    public ApiResponse<DepartmentDto> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return ApiResponse.ok("创建成功", departmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentDto> update(@PathVariable Long id,
                                              @Valid @RequestBody DepartmentUpdateRequest request) {
        return ApiResponse.ok("更新成功", departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
