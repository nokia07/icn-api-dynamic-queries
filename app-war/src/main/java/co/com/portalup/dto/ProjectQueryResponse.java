package co.com.portalup.dto;

import co.com.portalup.entity.ProjectQueryEntity;
import co.com.portalup.entity.ProjectQueryStatus;
import java.time.LocalDateTime;

public class ProjectQueryResponse {

    private final Long id;
    private final Long projectId;
    private final Long queryId;
    private final String config;
    private final ProjectQueryStatus status;
    private final LocalDateTime createdAt;

    public ProjectQueryResponse(Long id, Long projectId, Long queryId, String config, ProjectQueryStatus status,
                                LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.queryId = queryId;
        this.config = config;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ProjectQueryResponse from(ProjectQueryEntity entity) {
        return new ProjectQueryResponse(entity.getId(), entity.getProjectId(), entity.getQueryId(),
                entity.getConfig(), entity.getStatus(), entity.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getQueryId() {
        return queryId;
    }

    public String getConfig() {
        return config;
    }

    public ProjectQueryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
