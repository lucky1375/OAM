package com.oam.employee.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.employee.dto.EmployeeCreateRequest;
import com.oam.employee.dto.EmployeeDto;
import com.oam.employee.dto.EmployeeUpdateRequest;
import com.oam.employee.service.EmployeeService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ApiResponse<List<EmployeeDto>> list(@RequestParam(required = false) Long departmentId,
                                               @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(employeeService.list(departmentId, keyword));
    }

    @PostMapping
    public ApiResponse<EmployeeDto> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return ApiResponse.ok("员工创建成功", employeeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<EmployeeDto> update(@PathVariable Long id,
                                           @Valid @RequestBody EmployeeUpdateRequest request) {
        return ApiResponse.ok("更新成功", employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
