package com.oam.meeting.repository;

import com.oam.meeting.entity.UserGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, Long> {

    List<UserGroupMember> findByGroupId(Long groupId);

    void deleteByGroupId(Long groupId);
}
