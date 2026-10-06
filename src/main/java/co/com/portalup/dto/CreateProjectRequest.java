package co.com.portalup.dto;

import co.com.portalup.entity.ProjectStatus;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateProjectRequest {

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 100, message = "El nombre del proyecto no puede superar los 100 caracteres")
    private String name;

    @NotBlank(message = "La descripción del proyecto es obligatoria")
    @Size(max = 100, message = "La descripción del proyecto no puede superar los 100 caracteres")
    private String description;

    private ProjectStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
