package com.oam.position.repository;

import com.oam.position.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {

    boolean existsByDepartmentIdAndName(Long departmentId, String name);

    boolean existsByDepartmentIdAndNameAndIdNot(Long departmentId, String name, Long id);

    List<Position> findByDepartmentIdOrderByIdAsc(Long departmentId);

    List<Position> findAllByOrderByIdAsc();

    List<Position> findByNameContaining(String keyword);
}
