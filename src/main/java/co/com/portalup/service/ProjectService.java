package co.com.portalup.service;

import co.com.portalup.dto.CreateProjectRequest;
import co.com.portalup.dto.ProjectResponse;
import co.com.portalup.dto.UpdateProjectRequest;
import co.com.portalup.entity.ProjectEntity;
import co.com.portalup.entity.ProjectStatus;
import co.com.portalup.exception.ProjectNotFoundException;
import co.com.portalup.repository.ProjectRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        ProjectEntity project = new ProjectEntity();
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription().trim());
        project.setStatus(request.getStatus() == null ? ProjectStatus.ACTIVE : request.getStatus());
        return ProjectResponse.from(projectRepository.save(project));
    }

    public ProjectResponse findById(Long id) {
        return ProjectResponse.from(getProject(id));
    }

    public List<ProjectResponse> findAll(ProjectStatus status) {
        List<ProjectEntity> projects = status == null
                ? projectRepository.findAll()
                : projectRepository.findByStatus(status);
        return projects.stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Si status no se envía se conserva el actual. El id y la fecha de creación no forman
     * parte del request y la entidad los declara no actualizables, por lo que nunca cambian.
     */
    @Transactional
    public ProjectResponse update(Long id, UpdateProjectRequest request) {
        ProjectEntity project = getProject(id);
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription().trim());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        projectRepository.delete(getProject(id));
    }

    private ProjectEntity getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }
}
