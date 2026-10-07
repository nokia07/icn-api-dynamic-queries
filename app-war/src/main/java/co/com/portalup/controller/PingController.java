package co.com.portalup.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PingController {

    @GetMapping("/ping")
    public Map<String, String> verificarEstado() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "OK");
        respuesta.put("mensaje", "API ejecutándose exitosamente en WebSphere con Java 1.8");
        respuesta.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return respuesta;
    }
}
