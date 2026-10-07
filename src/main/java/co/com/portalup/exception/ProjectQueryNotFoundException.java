package co.com.portalup.exception;

public class ProjectQueryNotFoundException extends RuntimeException {

    public ProjectQueryNotFoundException(Long id) {
        super("No existe la asociación proyecto-consulta con id " + id);
    }
}
