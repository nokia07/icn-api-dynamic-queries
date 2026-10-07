package co.com.portalup.dto;

import co.com.portalup.entity.ProjectQueryStatus;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class CreateProjectQueryRequest {

    @NotNull(message = "El id del proyecto es obligatorio")
    @Positive(message = "El id del proyecto debe ser un número positivo")
    private Long projectId;

    @NotNull(message = "El id de la consulta es obligatorio")
    @Positive(message = "El id de la consulta debe ser un número positivo")
    private Long queryId;

    @Size(max = 1024, message = "La configuración no puede superar los 1024 caracteres")
    private String config;

    private ProjectQueryStatus status;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getQueryId() {
        return queryId;
    }

    public void setQueryId(Long queryId) {
        this.queryId = queryId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public ProjectQueryStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectQueryStatus status) {
        this.status = status;
    }
}
