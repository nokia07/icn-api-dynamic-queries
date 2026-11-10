package co.com.portalup.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Grupo con acceso al diseñador de consultas (docs/db/tbl_icn_designer_groups.sql). Solo se lee: los registros los
 * administra el DBA.
 */
@Entity
@Table(name = "tbl_icn_designer_groups")
public class DesignerGroupEntity {

    @Id
    @Column(name = "dgr_id", nullable = false, updatable = false)
    private Long id;

    /** Nombre corto (CN) o DN del grupo; se compara sin distinguir mayúsculas. */
    @Column(name = "dgr_name", nullable = false, length = 512)
    private String name;

    @Column(name = "dgr_description", length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "dgr_status", nullable = false, length = 10)
    private DesignerGroupStatus status;

    @Column(name = "dgr_created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DesignerGroupStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
