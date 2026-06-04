package edu.udla.integracion.progreso2.controller;

import edu.udla.integracion.progreso2.exception.CitaValidationException;
import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas Médicas", description = "Endpoints para registro y consulta de citas médicas")
public class CitaController {

    private static final Logger log = LoggerFactory.getLogger(CitaController.class);

    private final CitaValidationService citaValidationService;
    private final ProducerTemplate producerTemplate;

    public CitaController(CitaValidationService citaValidationService, ProducerTemplate producerTemplate) {
        this.citaValidationService = citaValidationService;
        this.producerTemplate = producerTemplate;
    }

    @Operation(
        summary = "Registrar una cita médica",
        description = "Recibe una cita médica, la valida y la envía al flujo de integración " +
                      "Camel → RabbitMQ (billing.queue, appointments.events, auditoria-citas.csv)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "202",
            description = "Cita recibida y en proceso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"mensaje\": \"Cita recibida y en proceso\", \"idCita\": \"C-001\"}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación — campo obligatorio faltante o valor inválido",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"El campo 'paciente' es obligatorio\"}")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Error interno del servidor\"}")
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos de la cita médica a registrar",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CitaRequest.class),
            examples = @ExampleObject(
                name = "Ejemplo de cita válida",
                value = """
                        {
                          "idCita": "C-001",
                          "paciente": "Juan Pérez",
                          "correo": "juan@mail.com",
                          "especialidad": "Cardiología",
                          "fechaCita": "2026-07-15",
                          "sede": "Norte",
                          "valor": 150.00
                        }
                        """
            )
        )
    )
    @PostMapping
    public ResponseEntity<?> crearCita(@RequestBody CitaRequest cita) {
        try {
            citaValidationService.validar(cita);

            log.info("[CitaController] Cita válida, enviando a Camel: id={}", cita.getIdCita());
            producerTemplate.sendBody("direct:procesarCita", cita);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                            "mensaje", "Cita recibida y en proceso",
                            "idCita", cita.getIdCita()
                    ));
        } catch (CitaValidationException e) {
            log.warn("[CitaController] Validación fallida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("[CitaController] Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Health check del servicio",
        description = "Verifica que el servicio de citas esté operativo"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Servicio activo",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"status\": \"UP\", \"servicio\": \"Progreso2 - Integración Citas Camel + RabbitMQ\"}")
        )
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "Progreso2 - Integración Citas Camel + RabbitMQ"
        ));
    }
}
