package com.oam.meeting.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class UserGroupRequest {

    @NotBlank(message = "请输入分组名称")
    @Size(max = 50, message = "分组名称不能超过50字")
    private String name;

    /** 分组成员账号ID */
    private List<Long> memberIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}
