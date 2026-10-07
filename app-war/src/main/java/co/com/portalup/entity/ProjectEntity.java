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

@Entity
@Table(name = "tbl_icn_projects")
public class ProjectEntity {

    /**
     * La columna es GENERATED ALWAYS AS IDENTITY: Oracle rechaza cualquier valor explícito,
     * por eso Hibernate la omite del INSERT y recupera el valor con getGeneratedKeys.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prj_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "prj_name", nullable = false, length = 100)
    private String name;

    @Column(name = "prj_description", nullable = false, length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "prj_status", nullable = false, length = 10)
    private ProjectStatus status;

    /**
     * El valor lo asigna el DEFAULT SYSTIMESTAMP de Oracle; Hibernate lo relee tras el INSERT
     * para que la respuesta de creación lo incluya, y nunca lo envía en INSERT ni UPDATE.
     */
    @Generated(GenerationTime.INSERT)
    @Column(name = "prj_created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
