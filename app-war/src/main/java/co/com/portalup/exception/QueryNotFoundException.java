package co.com.portalup.exception;

public class QueryNotFoundException extends RuntimeException {

    public QueryNotFoundException(Long id) {
        super("No existe la consulta con id " + id);
    }
}
