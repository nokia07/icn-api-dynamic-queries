package co.com.portalup.repository;

import co.com.portalup.entity.ProjectQueryEntity;
import co.com.portalup.entity.ProjectQueryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectQueryRepository extends JpaRepository<ProjectQueryEntity, Long> {

    boolean existsByProjectId(Long projectId);

    boolean existsByQueryId(Long queryId);

    boolean existsByProjectIdAndQueryId(Long projectId, Long queryId);

    boolean existsByProjectIdAndQueryIdAndIdNot(Long projectId, Long queryId, Long id);

    /** Cada filtro en null se ignora, así que sin filtros devuelve todas las asociaciones. */
    @Query("SELECT pq FROM ProjectQueryEntity pq"
            + " WHERE (:projectId IS NULL OR pq.projectId = :projectId)"
            + " AND (:queryId IS NULL OR pq.queryId = :queryId)"
            + " AND (:status IS NULL OR pq.status = :status)")
    List<ProjectQueryEntity> search(@Param("projectId") Long projectId,
                                    @Param("queryId") Long queryId,
                                    @Param("status") ProjectQueryStatus status);
}
