package co.com.portalup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * El plug-in de ICN llama a la API directamente desde el navegador, así que hay que permitir el origen de ICN.
 * Orígenes separados por coma en cors.allowed-origins.
 * Solo se registra si la propiedad existe (perfil local: ICN y la API en orígenes distintos). En WebSphere la API va
 * en el mismo host que ICN y no debe haber CORS, igual que /portalUpApp: con un proxy delante (IHS, puerto 444) Spring
 * ve otro host/puerto que el Origin del navegador, trata el POST como cross-origin y responde 403 "Invalid CORS request".
 */
@Configuration
@ConditionalOnProperty("cors.allowed-origins")
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
