package com.oam.department.repository;

import com.oam.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByParentIdAndName(Long parentId, String name);

    boolean existsByParentIdAndName(Long parentId, String name);

    boolean existsByParentIdAndNameAndIdNot(Long parentId, String name, Long id);

    boolean existsByParentId(Long parentId);

    List<Department> findByParentIdOrderBySortAscIdAsc(Long parentId);

    List<Department> findAllByOrderByLevelAscSortAscIdAsc();
}
