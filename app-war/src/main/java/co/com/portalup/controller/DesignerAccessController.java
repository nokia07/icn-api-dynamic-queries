package co.com.portalup.controller;

import co.com.portalup.security.DesignerAccessService;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Si el usuario actual puede usar el diseñador ({ canDesign }), con la misma regla que DesignerAccessInterceptor
 * aplica a las escrituras (tbl_icn_designer_groups). El plug-in de ICN lo usa para mostrar u ocultar el diseñador.
 */
@RestController
@RequestMapping("/api/v1/designer-access")
public class DesignerAccessController {

    private final DesignerAccessService designerAccessService;
    private final boolean enabled;

    public DesignerAccessController(DesignerAccessService designerAccessService,
                                    @Value("${dq.security.designer-check:true}") boolean enabled) {
        this.designerAccessService = designerAccessService;
        this.enabled = enabled;
    }

    @GetMapping
    public Map<String, Boolean> canDesign() throws Exception {
        // Sin la verificación (perfil local) la API deja escribir a todos: el diseñador también se muestra.
        return Collections.singletonMap("canDesign", !enabled || designerAccessService.canDesign());
    }
}
