package co.com.portalup.repository;

import co.com.portalup.entity.QueryEntity;
import co.com.portalup.entity.QueryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryRepository extends JpaRepository<QueryEntity, Long> {

    List<QueryEntity> findByStatus(QueryStatus status);
}
