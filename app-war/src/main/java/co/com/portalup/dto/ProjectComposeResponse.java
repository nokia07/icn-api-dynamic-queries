package co.com.portalup.dto;

import co.com.portalup.entity.ProjectEntity;
import co.com.portalup.entity.ProjectStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Proyecto con las consultas que tiene asociadas. */
public class ProjectComposeResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final ProjectStatus status;
    private final LocalDateTime createdAt;
    private final List<ComposedQueryResponse> queries;

    public ProjectComposeResponse(Long id, String name, String description, ProjectStatus status,
                                  LocalDateTime createdAt, List<ComposedQueryResponse> queries) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.queries = queries;
    }

    /** Crea el proyecto sin consultas; el servicio las agrega con {@link #getQueries()}. */
    public static ProjectComposeResponse from(ProjectEntity entity) {
        return new ProjectComposeResponse(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getStatus(), entity.getCreatedAt(), new ArrayList<>());
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

    public List<ComposedQueryResponse> getQueries() {
        return queries;
    }
}
