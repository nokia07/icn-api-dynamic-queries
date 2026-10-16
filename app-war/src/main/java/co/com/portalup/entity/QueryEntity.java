package co.com.portalup.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name = "tbl_icn_queries")
public class QueryEntity {

    /**
     * La columna es GENERATED AS IDENTITY (ALWAYS por defecto): Oracle rechaza cualquier valor explícito,
     * por eso Hibernate la omite del INSERT y recupera el valor con getGeneratedKeys.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qry_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "qry_name", nullable = false, length = 100)
    private String name;

    @Column(name = "qry_cesql", nullable = false, length = 4000)
    private String cesql;

    @Lob
    @Column(name = "qry_config", nullable = false)
    private String config;

    @Enumerated(EnumType.STRING)
    @Column(name = "qry_status", nullable = false, length = 10)
    private QueryStatus status;

    /**
     * El valor lo asigna el DEFAULT SYSTIMESTAMP de Oracle; Hibernate lo relee tras el INSERT
     * para que la respuesta de creación lo incluya, y nunca lo envía en INSERT ni UPDATE.
     */
    @Generated(GenerationTime.INSERT)
    @Column(name = "qry_created_at", nullable = false, insertable = false, updatable = false)
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
