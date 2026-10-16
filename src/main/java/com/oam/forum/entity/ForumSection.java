package com.oam.forum.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 论坛版块。
 */
@Entity
@Table(name = "frm_section")
public class ForumSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 版块名称 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 版块描述 */
    @Column(length = 200)
    private String description;

    /** 排序号（越小越靠前） */
    @Column(nullable = false)
    private Integer sort = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
