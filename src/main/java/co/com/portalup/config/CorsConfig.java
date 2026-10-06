package co.com.portalup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * El plug-in de ICN llama a la API directamente desde el navegador, así que hay que permitir el origen de ICN.
 * Orígenes separados por coma en cors.allowed-origins; vacío = no se permite ninguno.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${cors.allowed-origins:}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Content-Type", "Accept")
                .exposedHeaders("Location");
    }
}
