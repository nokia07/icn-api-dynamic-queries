package co.com.portalup.controller;

import co.com.portalup.dto.CreateProjectQueryRequest;
import co.com.portalup.dto.ProjectQueryResponse;
import co.com.portalup.dto.UpdateProjectQueryRequest;
import co.com.portalup.entity.ProjectQueryStatus;
import co.com.portalup.service.ProjectQueryService;
import java.net.URI;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
@RequestMapping("/api/v1/project-query")
public class ProjectQueryController {

    private static final String INVALID_ID = "El id de la asociación proyecto-consulta debe ser un número positivo";
    private static final String INVALID_PROJECT_ID = "El id del proyecto debe ser un número positivo";
    private static final String INVALID_QUERY_ID = "El id de la consulta debe ser un número positivo";

    private final ProjectQueryService projectQueryService;

    public ProjectQueryController(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    @PostMapping
    public ResponseEntity<ProjectQueryResponse> create(@Valid @RequestBody CreateProjectQueryRequest request) {
        ProjectQueryResponse created = projectQueryService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectQueryResponse> findById(@PathVariable @Positive(message = INVALID_ID) Long id) {
        return ResponseEntity.ok(projectQueryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProjectQueryResponse>> findAll(
            @RequestParam(required = false) @Positive(message = INVALID_PROJECT_ID) Long projectId,
            @RequestParam(required = false) @Positive(message = INVALID_QUERY_ID) Long queryId,
            @RequestParam(required = false) ProjectQueryStatus status) {
        return ResponseEntity.ok(projectQueryService.findAll(projectId, queryId, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectQueryResponse> update(@PathVariable @Positive(message = INVALID_ID) Long id,
                                                       @Valid @RequestBody UpdateProjectQueryRequest request) {
        return ResponseEntity.ok(projectQueryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = INVALID_ID) Long id) {
        projectQueryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
