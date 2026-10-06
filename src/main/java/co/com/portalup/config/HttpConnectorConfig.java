package co.com.portalup.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Conector HTTP adicional al HTTPS de server.port, solo con Tomcat embebido (perfil local). ICN necesita https por su
 * Content-Security-Policy, pero así Postman y otras pruebas pueden seguir usando http.
 */
@Configuration
@ConditionalOnProperty("server.http-port")
public class HttpConnectorConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final int httpPort;

    public HttpConnectorConfig(@Value("${server.http-port}") int httpPort) {
        this.httpPort = httpPort;
    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(httpPort);
        factory.addAdditionalTomcatConnectors(connector);
    }
}
