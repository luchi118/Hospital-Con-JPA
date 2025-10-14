package org.example.entidades;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.servicios.CitaException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "citas")
@Getter
@ToString(exclude = {"paciente", "medico", "sala"})
@NoArgsConstructor
public class Cita implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Column(length = 1000)
    private String observaciones;

    private Cita(CitaBuilder builder) {
        this.paciente = Objects.requireNonNull(builder.paciente, "El paciente no puede ser nulo");
        this.medico = Objects.requireNonNull(builder.medico, "El médico no puede ser nulo");
        this.sala = Objects.requireNonNull(builder.sala, "La sala no puede ser nula");
        this.fechaHora = Objects.requireNonNull(builder.fechaHora, "La fecha y hora no pueden ser nulas");
        this.costo = Objects.requireNonNull(builder.costo, "El costo no puede ser nulo");
        this.estado = builder.estado != null ? builder.estado : EstadoCita.PROGRAMADA;
        this.observaciones = builder.observaciones != null ? builder.observaciones : "";
    }

    public static class CitaBuilder {
        private Paciente paciente;
        private Medico medico;
        private Sala sala;
        private LocalDateTime fechaHora;
        private BigDecimal costo;
        private EstadoCita estado;
        private String observaciones;

        public CitaBuilder paciente(Paciente paciente) {
            this.paciente = paciente;
            return this;
        }

        public CitaBuilder medico(Medico medico) {
            this.medico = medico;
            return this;
        }

        public CitaBuilder sala(Sala sala) {
            this.sala = sala;
            return this;
        }

        public CitaBuilder fechaHora(LocalDateTime fechaHora) {
            this.fechaHora = fechaHora;
            return this;
        }

        public CitaBuilder costo(BigDecimal costo) {
            this.costo = costo;
            return this;
        }

        public CitaBuilder estado(EstadoCita estado) {
            this.estado = estado;
            return this;
        }

        public CitaBuilder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public Cita build() {
            return new Cita(this);
        }
    }

    public static CitaBuilder builder() {
        return new CitaBuilder();
    }

    // HS 19 default
    public void setEstado(EstadoCita estado) {
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
    }

    public void setObservaciones(String observaciones) {
        if(!observaciones.trim().equals("") && observaciones.trim().length() < 1000) {
        this.observaciones = observaciones;
        }
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                paciente.getDni(),
                medico.getDni(),
                sala.getNumero(),
                fechaHora.toString(),
                costo.toString(),
                estado.name(),
                observaciones.replaceAll(",", ";"));
    }

    /**
     * Construye una instancia de Cita a partir de una línea CSV.
     * <p>
     * Este método factory deserializa una línea CSV en una instancia de Cita,
     * resolviendo las referencias a Paciente, Medico y Sala mediante los mapas proporcionados.
     * </p>
     *
     * <p><b>Formato CSV esperado:</b></p>
     * <pre>
     * dniPaciente,dniMedico,numeroSala,fechaHora,costo,estado,observaciones
     * </pre>
     *
     * <p><b>Proceso de deserialización:</b></p>
     * <ol>
     *   <li>Divide la cadena CSV por comas</li>
     *   <li>Valida que haya exactamente 7 campos</li>
     *   <li>Parsea cada campo al tipo correspondiente</li>
     *   <li>Busca las entidades relacionadas en los mapas proporcionados</li>
     *   <li>Valida que todas las entidades existan</li>
     *   <li>Construye y retorna la cita</li>
     * </ol>
     *
     * <p><b>⚠️ CRÍTICO:</b> Los puntos y coma en las observaciones se reconvierten
     * a comas durante la deserialización.</p>
     *
     * <h3>Ejemplo de uso:</h3>
     * <pre>{@code
     * String csvLine = "12345678,87654321,S-201,2025-11-15T10:00:00,150.00,PROGRAMADA,Primera consulta";
     *
     * Map<String, Paciente> pacientes = new HashMap<>();
     * Map<String, Medico> medicos = new HashMap<>();
     * Map<String, Sala> salas = new HashMap<>();
     * // ... poblar los mapas ...
     *
     * Cita cita = Cita.fromCsvString(csvLine, pacientes, medicos, salas);
     * }</pre>
     *
     * @param csvString La línea CSV a deserializar
     * @param pacientes Mapa de DNI a Paciente para resolver referencias
     * @param medicos Mapa de DNI a Medico para resolver referencias
     * @param salas Mapa de número de sala a Sala para resolver referencias
     * @return Una nueva instancia de Cita construida desde el CSV
     * @throws CitaException si el formato CSV es inválido o las entidades no se encuentran
     * @see #toCsvString()
     */
    public static Cita fromCsvString(String csvString,
                                     Map<String, Paciente> pacientes,
                                     Map<String, Medico> medicos,
                                     Map<String, Sala> salas) throws CitaException {
        String[] values = csvString.split(",");
        if (values.length != 7) {
            throw new CitaException("Formato de CSV inválido para Cita: " + csvString);
        }

        String dniPaciente = values[0];
        String dniMedico = values[1];
        String numeroSala = values[2];
        LocalDateTime fechaHora = LocalDateTime.parse(values[3]);
        BigDecimal costo = new BigDecimal(values[4]);
        EstadoCita estado = EstadoCita.valueOf(values[5]);
        String observaciones = values[6].replaceAll(";", ",");

        Paciente paciente = pacientes.get(dniPaciente);
        Medico medico = medicos.get(dniMedico);
        Sala sala = salas.get(numeroSala);

        if (paciente == null) {
            throw new CitaException("Paciente no encontrado: " + dniPaciente);
        }
        if (medico == null) {
            throw new CitaException("Médico no encontrado: " + dniMedico);
        }
        if (sala == null) {
            throw new CitaException("Sala no encontrada: " + numeroSala);
        }

        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .sala(sala)
                .fechaHora(fechaHora)
                .costo(costo)
                .estado(estado)
                .observaciones(observaciones)
                .build();

        return cita;
    }
}
