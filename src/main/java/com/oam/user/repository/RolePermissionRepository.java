package com.oam.user.repository;

import com.oam.user.entity.Role;
import com.oam.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(Role role);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM RolePermission r WHERE r.role = :role")
    void deleteByRole(@Param("role") Role role);
}
