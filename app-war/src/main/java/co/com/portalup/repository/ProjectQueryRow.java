package co.com.portalup.repository;

import co.com.portalup.entity.ProjectEntity;
import co.com.portalup.entity.ProjectQueryEntity;
import co.com.portalup.entity.QueryEntity;

/** Fila del join asociación-proyecto-consulta que devuelve {@link ProjectQueryRepository#findAllComposed()}. */
public class ProjectQueryRow {

    private final ProjectQueryEntity projectQuery;
    private final ProjectEntity project;
    private final QueryEntity query;

    public ProjectQueryRow(ProjectQueryEntity projectQuery, ProjectEntity project, QueryEntity query) {
        this.projectQuery = projectQuery;
        this.project = project;
        this.query = query;
    }

    public ProjectQueryEntity getProjectQuery() {
        return projectQuery;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public QueryEntity getQuery() {
        return query;
    }
}
