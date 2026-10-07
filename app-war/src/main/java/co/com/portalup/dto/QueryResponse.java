package co.com.portalup.dto;

import co.com.portalup.entity.QueryEntity;
import co.com.portalup.entity.QueryStatus;
import java.time.LocalDateTime;

public class QueryResponse {

    private final Long id;
    private final String name;
    private final String cesql;
    private final String config;
    private final QueryStatus status;
    private final LocalDateTime createdAt;

    public QueryResponse(Long id, String name, String cesql, String config, QueryStatus status,
                         LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.cesql = cesql;
        this.config = config;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static QueryResponse from(QueryEntity entity) {
        return new QueryResponse(entity.getId(), entity.getName(), entity.getCesql(), entity.getConfig(),
                entity.getStatus(), entity.getCreatedAt());
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
}
