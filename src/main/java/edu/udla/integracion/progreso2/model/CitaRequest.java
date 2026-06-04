package edu.udla.integracion.progreso2.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos de una cita médica")
public class CitaRequest {

    @Schema(description = "Identificador único de la cita", example = "C-001")
    @JsonProperty("idCita")
    private String idCita;

    @Schema(description = "Nombre completo del paciente", example = "Juan Pérez")
    @JsonProperty("paciente")
    private String paciente;

    @Schema(description = "Correo electrónico del paciente", example = "juan@mail.com")
    @JsonProperty("correo")
    private String correo;

    @Schema(description = "Especialidad médica requerida", example = "Cardiología")
    @JsonProperty("especialidad")
    private String especialidad;

    @Schema(description = "Fecha de la cita en formato yyyy-MM-dd", example = "2026-07-15")
    @JsonProperty("fechaCita")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCita;

    @Schema(description = "Sede donde se realizará la cita", example = "Norte")
    @JsonProperty("sede")
    private String sede;

    @Schema(description = "Valor de la cita (debe ser mayor a 0)", example = "150.00")
    @JsonProperty("valor")
    private BigDecimal valor;

    public CitaRequest() {
    }

    public CitaRequest(String idCita, String paciente, String correo, String especialidad,
            LocalDate fechaCita, String sede, BigDecimal valor) {
        this.idCita = idCita;
        this.paciente = paciente;
        this.correo = correo;
        this.especialidad = especialidad;
        this.fechaCita = fechaCita;
        this.sede = sede;
        this.valor = valor;
    }

    public String getIdCita() {
        return idCita;
    }

    public void setIdCita(String idCita) {
        this.idCita = idCita;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public LocalDate getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(LocalDate fechaCita) {
        this.fechaCita = fechaCita;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "CitaRequest{idCita='" + idCita + "', paciente='" + paciente +
                "', correo='" + correo + "', especialidad='" + especialidad +
                "', fechaCita=" + fechaCita + ", sede='" + sede + "', valor=" + valor + "}";
    }
}
