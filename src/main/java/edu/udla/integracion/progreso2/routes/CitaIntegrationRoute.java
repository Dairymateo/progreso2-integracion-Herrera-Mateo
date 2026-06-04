package edu.udla.integracion.progreso2.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.udla.integracion.progreso2.exception.CitaValidationException;
import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CitaIntegrationRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(CitaIntegrationRoute.class);

    private final CitaValidationService citaValidationService;
    private final ObjectMapper objectMapper;

    @Value("${app.routes.outbox:data/outbox}")
    private String outboxDir;

    @Value("${app.routes.errors:data/errors}")
    private String errorsDir;

    private static final String EXCHANGE_BILLING = "spring-rabbitmq:billing.exchange?routingKey=billing.routing.key";
    private static final String EXCHANGE_EVENTS  = "spring-rabbitmq:appointments.events?exchangeType=fanout";

    public CitaIntegrationRoute(CitaValidationService citaValidationService, ObjectMapper objectMapper) {
        this.citaValidationService = citaValidationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() {

        onException(CitaValidationException.class, Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    CitaRequest cita = exchange.getProperty("citaOriginal", CitaRequest.class);

                    String idCita = "N/A";
                    String payloadStr = "N/A";

                    if (cita != null) {
                        idCita = cita.getIdCita() != null ? cita.getIdCita() : "N/A";
                        try {
                            payloadStr = objectMapper.writeValueAsString(cita);
                        } catch (Exception ignored) {
                            payloadStr = cita.toString();
                        }
                    }

                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String motivo = ex != null ? ex.getMessage() : "Error desconocido";
                    String logLine = "[" + timestamp + "] | idCita=" + idCita
                            + " | motivo=" + motivo
                            + " | payload=" + payloadStr + "\n";

                    exchange.getMessage().setBody(logLine);
                })
                .setHeader(Exchange.FILE_NAME, constant("citas-rechazadas.log"))
                .to("file://" + errorsDir + "?fileExist=Append")
                .log("⚠️ Cita rechazada registrada en citas-rechazadas.log");

        from("direct:procesarCita")
                .routeId("ruta-procesar-cita")
                .log("📩 Cita recibida para procesamiento")
                .setProperty("citaOriginal", body())
                .bean(citaValidationService, "validar")
                .log("✅ Cita válida, iniciando multicast")
                .multicast().parallelProcessing()
                    .to("direct:cita-facturacion", "direct:cita-pub-sub", "direct:cita-archivo")
                .end()
                .log("🎉 Multicast completado exitosamente");

        from("direct:cita-facturacion")
                .routeId("ruta-facturacion")
                .process(exchange -> {
                    CitaRequest cita = exchange.getMessage().getBody(CitaRequest.class);
                    Map<String, Object> billing = new LinkedHashMap<>();
                    billing.put("tipoMensaje", "COMANDO_FACTURAR_CITA");
                    billing.put("idCita", cita.getIdCita());
                    billing.put("paciente", cita.getPaciente());
                    billing.put("correo", cita.getCorreo());
                    billing.put("especialidad", cita.getEspecialidad());
                    billing.put("fechaCita", cita.getFechaCita() != null ? cita.getFechaCita().toString() : null);
                    billing.put("sede", cita.getSede());
                    billing.put("valor", cita.getValor());
                    exchange.getMessage().setBody(objectMapper.writeValueAsString(billing));
                })
                .to(EXCHANGE_BILLING)
                .log("💳 Mensaje enviado a billing.exchange (cola: billing.queue)");

        from("direct:cita-pub-sub")
                .routeId("ruta-pub-sub")
                .process(exchange -> {
                    CitaRequest cita = exchange.getMessage().getBody(CitaRequest.class);
                    Map<String, Object> evento = new LinkedHashMap<>();
                    evento.put("tipoEvento", "CITA_CONFIRMADA");
                    evento.put("idCita", cita.getIdCita());
                    evento.put("paciente", cita.getPaciente());
                    evento.put("correo", cita.getCorreo());
                    evento.put("especialidad", cita.getEspecialidad());
                    evento.put("fechaCita", cita.getFechaCita() != null ? cita.getFechaCita().toString() : null);
                    evento.put("sede", cita.getSede());
                    evento.put("valor", cita.getValor());
                    exchange.getMessage().setBody(objectMapper.writeValueAsString(evento));
                })
                .to(EXCHANGE_EVENTS)
                .log("📢 Evento CITA_CONFIRMADA publicado en appointments.events");

        from("direct:cita-archivo")
                .routeId("ruta-archivo-legado")
                .process(exchange -> {
                    CitaRequest cita = exchange.getMessage().getBody(CitaRequest.class);
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String csvLine = cita.getIdCita() + ","
                            + cita.getPaciente() + ","
                            + cita.getCorreo() + ","
                            + cita.getEspecialidad() + ","
                            + (cita.getFechaCita() != null ? cita.getFechaCita().toString() : "") + ","
                            + cita.getSede() + ","
                            + cita.getValor() + ","
                            + timestamp + "\n";
                    exchange.getMessage().setBody(csvLine);
                })
                .setHeader(Exchange.FILE_NAME, constant("auditoria-citas.csv"))
                .to("file://" + outboxDir + "?fileExist=Append")
                .log("📁 Cita agregada a auditoria-citas.csv");
    }
}
