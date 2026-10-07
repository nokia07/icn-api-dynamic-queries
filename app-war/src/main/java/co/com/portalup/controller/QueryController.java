package co.com.portalup.controller;

import co.com.portalup.dto.CreateQueryRequest;
import co.com.portalup.dto.QueryResponse;
import co.com.portalup.dto.UpdateQueryRequest;
import co.com.portalup.entity.QueryStatus;
import co.com.portalup.service.QueryService;
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
@RequestMapping("/api/v1/query")
public class QueryController {

    private static final String INVALID_ID = "El id de la consulta debe ser un número positivo";

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<QueryResponse> create(@Valid @RequestBody CreateQueryRequest request) {
        QueryResponse created = queryService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueryResponse> findById(@PathVariable @Positive(message = INVALID_ID) Long id) {
        return ResponseEntity.ok(queryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<QueryResponse>> findAll(@RequestParam(required = false) QueryStatus status) {
        return ResponseEntity.ok(queryService.findAll(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QueryResponse> update(@PathVariable @Positive(message = INVALID_ID) Long id,
                                                @Valid @RequestBody UpdateQueryRequest request) {
        return ResponseEntity.ok(queryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = INVALID_ID) Long id) {
        queryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
