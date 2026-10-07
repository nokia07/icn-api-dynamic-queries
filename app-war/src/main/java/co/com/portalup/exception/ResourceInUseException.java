package co.com.portalup.exception;

/** El recurso no se puede eliminar porque otros registros lo referencian por llave foránea. */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
