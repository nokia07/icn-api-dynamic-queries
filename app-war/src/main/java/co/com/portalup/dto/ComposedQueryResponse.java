package co.com.portalup.dto;

import co.com.portalup.entity.ProjectQueryStatus;
import co.com.portalup.entity.QueryStatus;
import co.com.portalup.repository.ProjectQueryRow;
import java.time.LocalDateTime;

/**
 * Consulta asociada a un proyecto: los datos de la consulta más los de la asociación,
 * que tiene su propia configuración y estado.
 */
public class ComposedQueryResponse {

    private final Long id;
    private final String name;
    private final String cesql;
    private final String config;
    private final QueryStatus status;
    private final LocalDateTime createdAt;
    private final Long projectQueryId;
    private final String projectQueryConfig;
    private final ProjectQueryStatus projectQueryStatus;

    public ComposedQueryResponse(Long id, String name, String cesql, String config, QueryStatus status,
                                 LocalDateTime createdAt, Long projectQueryId, String projectQueryConfig,
                                 ProjectQueryStatus projectQueryStatus) {
        this.id = id;
        this.name = name;
        this.cesql = cesql;
        this.config = config;
        this.status = status;
        this.createdAt = createdAt;
        this.projectQueryId = projectQueryId;
        this.projectQueryConfig = projectQueryConfig;
        this.projectQueryStatus = projectQueryStatus;
    }

    public static ComposedQueryResponse from(ProjectQueryRow row) {
        return new ComposedQueryResponse(row.getQuery().getId(), row.getQuery().getName(), row.getQuery().getCesql(),
                row.getQuery().getConfig(), row.getQuery().getStatus(), row.getQuery().getCreatedAt(),
                row.getProjectQuery().getId(), row.getProjectQuery().getConfig(), row.getProjectQuery().getStatus());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCesql() {
        return cesql;
    }

    public String getConfig() {
        return config;
    }

    public QueryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getProjectQueryId() {
        return projectQueryId;
    }

    public String getProjectQueryConfig() {
        return projectQueryConfig;
    }

    public ProjectQueryStatus getProjectQueryStatus() {
        return projectQueryStatus;
    }
}
