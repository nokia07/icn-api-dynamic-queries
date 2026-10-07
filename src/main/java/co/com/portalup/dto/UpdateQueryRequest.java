package co.com.portalup.dto;

import co.com.portalup.entity.QueryStatus;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdateQueryRequest {

    @NotBlank(message = "El nombre de la consulta es obligatorio")
    @Size(max = 100, message = "El nombre de la consulta no puede superar los 100 caracteres")
    private String name;

    @NotBlank(message = "La sentencia CE SQL de la consulta es obligatoria")
    @Size(max = 512, message = "La sentencia CE SQL de la consulta no puede superar los 512 caracteres")
    private String cesql;

    @NotBlank(message = "La configuración de la consulta es obligatoria")
    @Size(max = 1024, message = "La configuración de la consulta no puede superar los 1024 caracteres")
    private String config;

    private QueryStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCesql() {
        return cesql;
    }

    public void setCesql(String cesql) {
        this.cesql = cesql;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public QueryStatus getStatus() {
        return status;
    }

    public void setStatus(QueryStatus status) {
        this.status = status;
    }
}
