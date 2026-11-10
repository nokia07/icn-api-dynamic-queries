package co.com.portalup.security;

import co.com.portalup.exception.DesignerAccessDeniedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Las escrituras (todo lo que no es GET, HEAD ni OPTIONS) solo para los usuarios de los grupos del diseñador
 * (DesignerAccessService). Leer basta con estar autenticado (web.xml): "Resultados de consultas" lo usan todos.
 */
public class DesignerAccessInterceptor implements HandlerInterceptor {

    private static final Log LOG = LogFactory.getLog(DesignerAccessInterceptor.class);

    private final DesignerAccessService designerAccessService;

    public DesignerAccessInterceptor(DesignerAccessService designerAccessService) {
        this.designerAccessService = designerAccessService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }
        if (!designerAccessService.canDesign()) {
            LOG.info("Escritura rechazada: " + request.getRemoteUser() + " no pertenece a un grupo del diseñador ("
                    + method + " " + request.getRequestURI() + ")");
            throw new DesignerAccessDeniedException(
                    "El usuario no pertenece a un grupo con acceso al diseñador de consultas");
        }
        return true;
    }
}
