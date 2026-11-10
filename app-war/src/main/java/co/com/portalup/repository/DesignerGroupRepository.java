package co.com.portalup.repository;

import co.com.portalup.entity.DesignerGroupEntity;
import co.com.portalup.entity.DesignerGroupStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DesignerGroupRepository extends JpaRepository<DesignerGroupEntity, Long> {

    @Query("select g.name from DesignerGroupEntity g where g.status = :status")
    List<String> findNamesByStatus(@Param("status") DesignerGroupStatus status);
}
