package co.com.portalup.exception;

public class DuplicateProjectQueryException extends RuntimeException {

    public DuplicateProjectQueryException(Long projectId, Long queryId) {
        super("La consulta con id " + queryId + " ya está asociada al proyecto con id " + projectId);
    }
}
