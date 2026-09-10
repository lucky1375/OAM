package com.oam.user.repository;

import com.oam.user.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
