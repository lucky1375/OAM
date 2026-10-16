package com.oam.meeting.service;

import com.oam.common.security.SecurityUtil;
import com.oam.meeting.dto.UserGroupDto;
import com.oam.meeting.dto.UserGroupRequest;
import com.oam.meeting.entity.UserGroup;
import com.oam.meeting.entity.UserGroupMember;
import com.oam.meeting.repository.UserGroupMemberRepository;
import com.oam.meeting.repository.UserGroupRepository;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人账号分组：仅本人可见、可管理，用于预约会议时整组选入参会人。
 */
@Service
public class UserGroupService {

    private final UserGroupRepository groupRepository;
    private final UserGroupMemberRepository memberRepository;
    private final UserRepository userRepository;

    public UserGroupService(UserGroupRepository groupRepository,
                            UserGroupMemberRepository memberRepository,
                            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public List<UserGroupDto> listGroups() {
        Long ownerId = currentUserId();
        if (ownerId == null) return new ArrayList<>();
        return groupRepository.findByOwnerUserIdOrderByIdAsc(ownerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserGroupDto create(UserGroupRequest request) {
        Long ownerId = requireCurrentUserId();
        String name = request.getName().trim();
        if (!groupRepository.findByOwnerUserIdOrderByIdAsc(ownerId).stream()
                .noneMatch(g -> g.getName().equals(name))) {
            throw new IllegalArgumentException("分组「" + name + "」已存在");
        }
        UserGroup g = new UserGroup();
        g.setOwnerUserId(ownerId);
        g.setName(name);
        g.setCreatedBy(SecurityUtil.currentUsername());
        g = groupRepository.save(g);
        replaceMembers(g.getId(), request.getMemberIds());
        return toDto(g);
    }

    @Transactional
    public UserGroupDto update(Long id, UserGroupRequest request) {
        Long ownerId = requireCurrentUserId();
        UserGroup g = groupRepository.findByIdAndOwnerUserId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("分组不存在"));
        String name = request.getName().trim();
        if (groupRepository.existsByOwnerUserIdAndNameAndIdNot(ownerId, name, id)) {
            throw new IllegalArgumentException("分组「" + name + "」已存在");
        }
        g.setName(name);
        groupRepository.save(g);
        replaceMembers(id, request.getMemberIds());
        return toDto(g);
    }

    @Transactional
    public void delete(Long id) {
        Long ownerId = requireCurrentUserId();
        UserGroup g = groupRepository.findByIdAndOwnerUserId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("分组不存在"));
        memberRepository.deleteByGroupId(id);
        groupRepository.delete(g);
    }

    // ==================== 辅助 ====================

    private void replaceMembers(Long groupId, List<Long> memberIds) {
        memberRepository.deleteByGroupId(groupId);
        // 先落库删除，避免与随后插入的同名(groupId,userId)记录冲突
        memberRepository.flush();
        if (memberIds == null) return;
        Set<Long> unique = new LinkedHashSet<>(memberIds);
        for (Long uid : unique) {
            if (uid == null) continue;
            UserGroupMember m = new UserGroupMember();
            m.setGroupId(groupId);
            m.setUserId(uid);
            memberRepository.save(m);
        }
    }

    private UserGroupDto toDto(UserGroup g) {
        List<Long> ids = memberRepository.findByGroupId(g.getId()).stream()
                .map(UserGroupMember::getUserId)
                .collect(Collectors.toList());
        List<String> names = new ArrayList<>();
        for (Long uid : ids) {
            User u = userRepository.findById(uid).orElse(null);
            names.add(u != null
                    ? (u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername())
                    : ("账号" + uid));
        }
        UserGroupDto dto = new UserGroupDto();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setMemberIds(ids);
        dto.setMemberNames(names);
        dto.setMemberCount(ids.size());
        return dto;
    }

    private Long requireCurrentUserId() {
        Long id = currentUserId();
        if (id == null) {
            throw new IllegalArgumentException("未登录");
        }
        return id;
    }

    private Long currentUserId() {
        String username = SecurityUtil.currentUsername();
        if (username == null) return null;
        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }
}
