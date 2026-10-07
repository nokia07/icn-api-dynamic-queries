package co.com.portalup.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

/**
 * Asociación de una consulta a un proyecto. Los ids se mapean como columnas simples (sin @ManyToOne);
 * en BD son llaves foráneas sin cascada y la pareja proyecto-consulta es única (uq_icn_project_queries).
 */
@Entity
@Table(name = "tbl_icn_project_queries")
public class ProjectQueryEntity {

    /**
     * La columna es GENERATED AS IDENTITY (ALWAYS por defecto): Oracle rechaza cualquier valor explícito,
     * por eso Hibernate la omite del INSERT y recupera el valor con getGeneratedKeys.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prj_qry_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "prj_qry_id_project", nullable = false)
    private Long projectId;

    @Column(name = "prj_qry_id_query", nullable = false)
    private Long queryId;

    @Column(name = "prj_qry_config", length = 1024)
    private String config;

    @Enumerated(EnumType.STRING)
    @Column(name = "prj_qry_status", nullable = false, length = 10)
    private ProjectQueryStatus status;

    /**
     * El valor lo asigna el DEFAULT SYSTIMESTAMP de Oracle; Hibernate lo relee tras el INSERT
     * para que la respuesta de creación lo incluya, y nunca lo envía en INSERT ni UPDATE.
     */
    @Generated(GenerationTime.INSERT)
    @Column(name = "prj_qry_created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
