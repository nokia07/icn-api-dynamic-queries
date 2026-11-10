package co.com.portalup.exception;

/** El usuario no pertenece a ningún grupo activo de tbl_icn_designer_groups. */
public class DesignerAccessDeniedException extends RuntimeException {

    public DesignerAccessDeniedException(String message) {
        super(message);
    }
}
