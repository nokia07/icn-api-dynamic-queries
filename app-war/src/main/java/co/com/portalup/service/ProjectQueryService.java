package co.com.portalup.service;

import co.com.portalup.dto.ComposedQueryResponse;
import co.com.portalup.dto.CreateProjectQueryRequest;
import co.com.portalup.dto.ProjectComposeResponse;
import co.com.portalup.dto.ProjectQueryResponse;
import co.com.portalup.dto.UpdateProjectQueryRequest;
import co.com.portalup.entity.ProjectQueryEntity;
import co.com.portalup.entity.ProjectQueryStatus;
import co.com.portalup.exception.DuplicateProjectQueryException;
import co.com.portalup.exception.ProjectNotFoundException;
import co.com.portalup.exception.ProjectQueryNotFoundException;
import co.com.portalup.exception.QueryNotFoundException;
import co.com.portalup.repository.ProjectQueryRepository;
import co.com.portalup.repository.ProjectQueryRow;
import co.com.portalup.repository.ProjectRepository;
import co.com.portalup.repository.QueryRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectQueryService {

    private final ProjectQueryRepository projectQueryRepository;
    private final ProjectRepository projectRepository;
    private final QueryRepository queryRepository;

    public ProjectQueryService(ProjectQueryRepository projectQueryRepository, ProjectRepository projectRepository,
                               QueryRepository queryRepository) {
        this.projectQueryRepository = projectQueryRepository;
        this.projectRepository = projectRepository;
        this.queryRepository = queryRepository;
    }

    @Transactional
    public ProjectQueryResponse create(CreateProjectQueryRequest request) {
        validateReferences(request.getProjectId(), request.getQueryId());
        if (projectQueryRepository.existsByProjectIdAndQueryId(request.getProjectId(), request.getQueryId())) {
            throw new DuplicateProjectQueryException(request.getProjectId(), request.getQueryId());
        }
        ProjectQueryEntity projectQuery = new ProjectQueryEntity();
        projectQuery.setProjectId(request.getProjectId());
        projectQuery.setQueryId(request.getQueryId());
        projectQuery.setConfig(normalize(request.getConfig()));
        projectQuery.setStatus(request.getStatus() == null ? ProjectQueryStatus.ACTIVE : request.getStatus());
        return ProjectQueryResponse.from(projectQueryRepository.save(projectQuery));
    }

    public ProjectQueryResponse findById(Long id) {
        return ProjectQueryResponse.from(getProjectQuery(id));
    }

    public List<ProjectQueryResponse> findAll(Long projectId, Long queryId, ProjectQueryStatus status) {
        return projectQueryRepository.search(projectId, queryId, status).stream()
                .map(ProjectQueryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Agrupa las asociaciones por proyecto. Solo aparecen los proyectos con al menos una consulta
     * asociada; el orden (proyecto y luego asociación) lo da la consulta del repositorio.
     */
    public List<ProjectComposeResponse> findAllComposed() {
        Map<Long, ProjectComposeResponse> projects = new LinkedHashMap<>();
        for (ProjectQueryRow row : projectQueryRepository.findAllComposed()) {
            projects.computeIfAbsent(row.getProject().getId(), id -> ProjectComposeResponse.from(row.getProject()))
                    .getQueries()
                    .add(ComposedQueryResponse.from(row));
        }
        return new ArrayList<>(projects.values());
    }

    /**
     * Si status no se envía se conserva el actual. El id y la fecha de creación no forman
     * parte del request y la entidad los declara no actualizables, por lo que nunca cambian.
     */
    @Transactional
    public ProjectQueryResponse update(Long id, UpdateProjectQueryRequest request) {
        ProjectQueryEntity projectQuery = getProjectQuery(id);
        validateReferences(request.getProjectId(), request.getQueryId());
        if (projectQueryRepository.existsByProjectIdAndQueryIdAndIdNot(
                request.getProjectId(), request.getQueryId(), id)) {
            throw new DuplicateProjectQueryException(request.getProjectId(), request.getQueryId());
        }
        projectQuery.setProjectId(request.getProjectId());
        projectQuery.setQueryId(request.getQueryId());
        projectQuery.setConfig(normalize(request.getConfig()));
        if (request.getStatus() != null) {
            projectQuery.setStatus(request.getStatus());
        }
        return ProjectQueryResponse.from(projectQueryRepository.save(projectQuery));
    }

    @Transactional
    public void delete(Long id) {
        projectQueryRepository.delete(getProjectQuery(id));
    }

    private ProjectQueryEntity getProjectQuery(Long id) {
        return projectQueryRepository.findById(id)
                .orElseThrow(() -> new ProjectQueryNotFoundException(id));
    }

    /**
     * Las llaves foráneas ya lo garantizan, pero se valida antes de guardar para responder 404 con un
     * mensaje claro en lugar del error genérico de integridad. Igual con la unicidad proyecto-consulta
     * (uq_icn_project_queries): si dos peticiones concurrentes pasan la validación, la constraint
     * rechaza la segunda y termina en el 409 genérico de DataIntegrityViolationException.
     */
    private void validateReferences(Long projectId, Long queryId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }
        if (!queryRepository.existsById(queryId)) {
            throw new QueryNotFoundException(queryId);
        }
    }

    /** La configuración es opcional: un texto en blanco se guarda como null (Oracle trata '' como NULL). */
    private static String normalize(String config) {
        if (config == null || config.trim().isEmpty()) {
            return null;
        }
        return config.trim();
    }
}
