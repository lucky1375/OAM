package com.oam.forum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SectionCreateRequest {

    @NotBlank(message = "版块名称不能为空")
    @Size(max = 50, message = "名称不能超过50字")
    private String name;

    @Size(max = 200, message = "描述不能超过200字")
    private String description;

    private Integer sort = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
