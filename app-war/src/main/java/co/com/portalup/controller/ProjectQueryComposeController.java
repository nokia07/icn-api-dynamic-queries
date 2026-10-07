package co.com.portalup.controller;

import co.com.portalup.dto.ProjectComposeResponse;
import co.com.portalup.service.ProjectQueryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Vista de solo lectura de tbl_icn_project_queries agrupada por proyecto. */
@RestController
@RequestMapping("/api/v1/project-query-compose")
public class ProjectQueryComposeController {

    private final ProjectQueryService projectQueryService;

    public ProjectQueryComposeController(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectComposeResponse>> findAll() {
        return ResponseEntity.ok(projectQueryService.findAllComposed());
    }
}
