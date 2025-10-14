package org.example.entidades;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "historias_clinicas")
@Getter
@ToString(exclude = {"paciente"})
@NoArgsConstructor
public class HistoriaClinica implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_historia", nullable = false, unique = true, length = 50)
    private String numeroHistoria;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "diagnosticos", joinColumns = @JoinColumn(name = "historia_clinica_id"))
    @Column(name = "diagnostico", length = 500)
    private List<String> diagnosticos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tratamientos", joinColumns = @JoinColumn(name = "historia_clinica_id"))
    @Column(name = "tratamiento", length = 500)
    private List<String> tratamientos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "alergias", joinColumns = @JoinColumn(name = "historia_clinica_id"))
    @Column(name = "alergia", length = 200)
    private List<String> alergias = new ArrayList<>();

    private HistoriaClinica(HistoriaClinicaBuilder builder) {
        this.diagnosticos = new ArrayList<>();
        this.tratamientos = new ArrayList<>();
        this.alergias = new ArrayList<>();
        this.paciente = Objects.requireNonNull(builder.paciente, "El paciente no puede ser nulo");
        this.fechaCreacion = builder.fechaCreacion != null ? builder.fechaCreacion : LocalDateTime.now();
        this.numeroHistoria = generarNumeroHistoria();
    }

    public static class HistoriaClinicaBuilder {
        private Paciente paciente;
        private LocalDateTime fechaCreacion;


        public HistoriaClinicaBuilder paciente(Paciente paciente) {
            this.paciente = paciente;
            return this;
        }

        public HistoriaClinicaBuilder fechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public HistoriaClinica build() {
            return new HistoriaClinica(this);
        }
    }

    public static HistoriaClinicaBuilder builder() {
        return new HistoriaClinicaBuilder();
    }


    private String generarNumeroHistoria() {
        return "HC-" + paciente.getDni() + "-" + System.currentTimeMillis();
    }

    public void agregarDiagnostico(String diagnostico) {
        if (diagnostico != null && !diagnostico.trim().isEmpty() && diagnostico.length() < 500) {
            diagnosticos.add(diagnostico);
        }
    }

    public void agregarTratamiento(String tratamiento) {
        if (tratamiento != null && !tratamiento.trim().isEmpty() &&  tratamiento.length() < 500) {
            tratamientos.add(tratamiento);
        }
    }

    public void agregarAlergia(String alergia) {
        if (alergia != null && !alergia.trim().isEmpty() && alergia.length() < 200) {
            alergias.add(alergia);
        }
    }

    public List<String> getDiagnosticos() {
        return Collections.unmodifiableList(diagnosticos);
    }

    public List<String> getTratamientos() {
        return Collections.unmodifiableList(tratamientos);
    }

    public List<String> getAlergias() {
        return Collections.unmodifiableList(alergias);
    }

    // Builder estático o método fábrica
    public static HistoriaClinica crearParaPaciente(Paciente paciente) {
        HistoriaClinica hc = new HistoriaClinica();
        hc.paciente = paciente;
        hc.fechaCreacion = LocalDateTime.now();
        hc.numeroHistoria = "HC-" + paciente.getDni() + "-" + System.currentTimeMillis();
        return hc;
    }

}
