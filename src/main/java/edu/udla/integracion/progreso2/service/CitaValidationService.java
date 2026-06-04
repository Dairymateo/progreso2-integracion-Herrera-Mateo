package edu.udla.integracion.progreso2.service;

import edu.udla.integracion.progreso2.exception.CitaValidationException;
import edu.udla.integracion.progreso2.model.CitaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CitaValidationService {

    private static final Logger log = LoggerFactory.getLogger(CitaValidationService.class);

    public void validar(CitaRequest cita) {
        log.info("[CitaValidationService] Validando cita id={}", cita.getIdCita());

        if (isBlank(cita.getIdCita())) {
            throw new CitaValidationException("El campo 'idCita' es obligatorio");
        }
        if (isBlank(cita.getPaciente())) {
            throw new CitaValidationException("El campo 'paciente' es obligatorio");
        }
        if (isBlank(cita.getCorreo())) {
            throw new CitaValidationException("El campo 'correo' es obligatorio");
        }
        if (isBlank(cita.getEspecialidad())) {
            throw new CitaValidationException("El campo 'especialidad' es obligatorio");
        }
        if (cita.getFechaCita() == null) {
            throw new CitaValidationException("El campo 'fechaCita' es obligatorio");
        }
        if (isBlank(cita.getSede())) {
            throw new CitaValidationException("El campo 'sede' es obligatorio");
        }
        if (cita.getValor() == null || cita.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CitaValidationException("El campo 'valor' debe ser mayor a 0");
        }

        log.info("[CitaValidationService] Cita id={} validada correctamente", cita.getIdCita());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
