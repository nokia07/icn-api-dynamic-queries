package co.com.portalup.service;

import co.com.portalup.dto.CreateQueryRequest;
import co.com.portalup.dto.QueryResponse;
import co.com.portalup.dto.UpdateQueryRequest;
import co.com.portalup.entity.QueryEntity;
import co.com.portalup.entity.QueryStatus;
import co.com.portalup.exception.QueryNotFoundException;
import co.com.portalup.exception.ResourceInUseException;
import co.com.portalup.repository.ProjectQueryRepository;
import co.com.portalup.repository.QueryRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QueryService {

    private final QueryRepository queryRepository;
    private final ProjectQueryRepository projectQueryRepository;

    public QueryService(QueryRepository queryRepository, ProjectQueryRepository projectQueryRepository) {
        this.queryRepository = queryRepository;
        this.projectQueryRepository = projectQueryRepository;
    }

    @Transactional
    public QueryResponse create(CreateQueryRequest request) {
        QueryEntity query = new QueryEntity();
        query.setName(request.getName().trim());
        query.setCesql(request.getCesql().trim());
        query.setConfig(request.getConfig().trim());
        query.setStatus(request.getStatus() == null ? QueryStatus.ACTIVE : request.getStatus());
        return QueryResponse.from(queryRepository.save(query));
    }

    public QueryResponse findById(Long id) {
        return QueryResponse.from(getQuery(id));
    }

    public List<QueryResponse> findAll(QueryStatus status) {
        List<QueryEntity> queries = status == null
                ? queryRepository.findAll()
                : queryRepository.findByStatus(status);
        return queries.stream()
                .map(QueryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Si status no se envía se conserva el actual. El id y la fecha de creación no forman
     * parte del request y la entidad los declara no actualizables, por lo que nunca cambian.
     */
    @Transactional
    public QueryResponse update(Long id, UpdateQueryRequest request) {
        QueryEntity query = getQuery(id);
        query.setName(request.getName().trim());
        query.setCesql(request.getCesql().trim());
        query.setConfig(request.getConfig().trim());
        if (request.getStatus() != null) {
            query.setStatus(request.getStatus());
        }
        return QueryResponse.from(queryRepository.save(query));
    }

    /**
     * fk_icn_project_queries_query no tiene ON DELETE CASCADE: se valida antes de borrar para
     * responder 409 con un mensaje claro en lugar del error genérico de integridad.
     */
    @Transactional
    public void delete(Long id) {
        QueryEntity query = getQuery(id);
        if (projectQueryRepository.existsByQueryId(id)) {
            throw new ResourceInUseException("No se puede eliminar la consulta con id " + id
                    + " porque está asociada a uno o más proyectos");
        }
        queryRepository.delete(query);
    }

    private QueryEntity getQuery(Long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new QueryNotFoundException(id));
    }
}
