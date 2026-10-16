package com.oam.forum.dto;

import com.oam.forum.entity.ForumSection;

public class SectionDto {

    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private Long postCount;

    public static SectionDto from(ForumSection s) {
        return from(s, 0L);
    }

    public static SectionDto from(ForumSection s, Long postCount) {
        SectionDto dto = new SectionDto();
        dto.id = s.getId();
        dto.name = s.getName();
        dto.description = s.getDescription();
        dto.sort = s.getSort();
        dto.postCount = postCount;
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Long getPostCount() { return postCount; }
    public void setPostCount(Long postCount) { this.postCount = postCount; }
}
