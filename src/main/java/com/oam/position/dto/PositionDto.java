package com.oam.position.dto;

import com.oam.position.entity.Position;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PositionDto {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
    private Boolean enabled;
    private String createdAt;
    private String createdBy;
    private String updatedBy;
    private String updatedAt;

    public static PositionDto from(Position p, String departmentName) {
        PositionDto dto = new PositionDto();
        dto.id = p.getId();
        dto.name = p.getName();
        dto.description = p.getDescription();
        dto.departmentId = p.getDepartmentId();
        dto.departmentName = departmentName;
        dto.enabled = p.getEnabled();
        dto.createdAt = p.getCreatedAt() == null ? null : p.getCreatedAt().format(FMT);
        dto.createdBy = p.getCreatedBy();
        dto.updatedBy = p.getUpdatedBy();
        dto.updatedAt = p.getUpdatedAt() == null ? null : p.getUpdatedAt().format(FMT);
        return dto;
    }

    public static String fmt(java.time.LocalDateTime t) {
        return t == null ? null : t.format(FMT);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static List<PositionDto> decorate(List<Position> positions, java.util.function.Function<Long, String> deptNameResolver) {
        return positions.stream()
                .map(p -> from(p, deptNameResolver.apply(p.getDepartmentId())))
                .collect(Collectors.toList());
    }
}
