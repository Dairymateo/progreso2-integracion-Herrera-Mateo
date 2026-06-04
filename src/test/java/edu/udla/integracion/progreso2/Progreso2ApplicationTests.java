package edu.udla.integracion.progreso2;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("test")
class Progreso2ApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring y Camel levantan correctamente
    }

}
