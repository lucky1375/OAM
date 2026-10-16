package com.oam.meeting.dto;

import java.util.List;

/** 账号分组（含成员，供参会人整组选择） */
public class UserGroupDto {

    private Long id;
    private String name;
    private Integer memberCount;
    private List<Long> memberIds;
    private List<String> memberNames;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
    public List<String> getMemberNames() { return memberNames; }
    public void setMemberNames(List<String> memberNames) { this.memberNames = memberNames; }
}
