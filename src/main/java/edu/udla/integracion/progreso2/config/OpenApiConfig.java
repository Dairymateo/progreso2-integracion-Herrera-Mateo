package edu.udla.integracion.progreso2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Progreso 2 - API de Gestión de Citas Médicas")
                        .version("1.0.0")
                        .description("API REST para el registro y procesamiento de citas médicas " +
                                "mediante integración con Apache Camel y RabbitMQ."));
    }
}
