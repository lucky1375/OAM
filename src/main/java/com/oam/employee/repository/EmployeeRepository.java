package com.oam.employee.repository;

import com.oam.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserId(Long userId);

    boolean existsByEmployeeNo(String employeeNo);

    List<Employee> findByDepartmentId(Long departmentId);
}
