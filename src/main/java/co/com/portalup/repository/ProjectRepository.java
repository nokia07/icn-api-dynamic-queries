package co.com.portalup.repository;

import co.com.portalup.entity.ProjectEntity;
import co.com.portalup.entity.ProjectStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findByStatus(ProjectStatus status);
}
