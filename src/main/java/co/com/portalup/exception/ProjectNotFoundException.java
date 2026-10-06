package co.com.portalup.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(Long id) {
        super("No existe el proyecto con id " + id);
    }
}
