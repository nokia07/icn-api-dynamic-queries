package co.com.portalup.dto;

import co.com.portalup.entity.ProjectEntity;
import co.com.portalup.entity.ProjectStatus;
import java.time.LocalDateTime;

public class ProjectResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final ProjectStatus status;
    private final LocalDateTime createdAt;

    public ProjectResponse(Long id, String name, String description, ProjectStatus status,
                           LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ProjectResponse from(ProjectEntity entity) {
        return new ProjectResponse(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getStatus(), entity.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
