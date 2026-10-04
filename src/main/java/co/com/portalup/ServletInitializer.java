package co.com.portalup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ServletInitializer extends SpringBootServletInitializer {

    // Método requerido para el despliegue en WebSphere (servidor externo)
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ServletInitializer.class);
    }

    // Permite ejecutar la aplicación localmente de forma independiente si fuera necesario
    public static void main(String[] args) {
        SpringApplication.run(ServletInitializer.class, args);
    }
}
