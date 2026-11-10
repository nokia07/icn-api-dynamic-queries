package co.com.portalup.config;

import co.com.portalup.security.DesignerAccessInterceptor;
import co.com.portalup.security.DesignerAccessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra DesignerAccessInterceptor sobre /api/**. dq.security.designer-check=false lo desactiva: solo para el perfil
 * local, donde la API no tiene usuario (el Tomcat embebido no aplica web.xml).
 */
@Configuration
public class DesignerAccessConfig implements WebMvcConfigurer {

    private final DesignerAccessService designerAccessService;
    private final boolean enabled;

    public DesignerAccessConfig(DesignerAccessService designerAccessService,
                                @Value("${dq.security.designer-check:true}") boolean enabled) {
        this.designerAccessService = designerAccessService;
        this.enabled = enabled;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (enabled) {
            registry.addInterceptor(new DesignerAccessInterceptor(designerAccessService)).addPathPatterns("/api/**");
        }
    }
}
