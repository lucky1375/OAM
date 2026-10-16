package com.oam.meeting.repository;

import com.oam.meeting.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    List<UserGroup> findByOwnerUserIdOrderByIdAsc(Long ownerUserId);

    Optional<UserGroup> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    boolean existsByOwnerUserIdAndNameAndIdNot(Long ownerUserId, String name, Long id);
}
