package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "departamentos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EspecialidadMedica especialidad;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @OneToMany(mappedBy = "departamento",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private List<Medico> medicos = new ArrayList<>();

    @OneToMany(mappedBy = "departamento",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private List<Sala> salas = new ArrayList<>();

    @Builder
    protected Departamento(String nombre, EspecialidadMedica especialidad) {
        this.nombre = Objects.requireNonNull(nombre, "Nombre no puede ser nulo");
        this.especialidad = Objects.requireNonNull(especialidad, "Especialidad no puede ser nula");
    }

    public void agregarMedico(Medico medico) {
        Objects.requireNonNull(medico, "Medico no puede ser null");
        if (medico.getEspecialidad() == null) {
            throw new IllegalArgumentException("Médico debe tener especialidad");
        }
        if (!medico.getEspecialidad().equals(this.especialidad)) {
            throw new IllegalArgumentException("Especialidad incompatible");
        }
        medico.setDepartamento(this);
        medicos.add(medico);
    }

    public Sala crearSala(String numero, String tipo) {
        Sala s = Sala.builder()
                .numero(numero)
                .tipo(tipo)
                .build();
        s.setDepartamento(this);
        this.salas.add(s);
        return s;
    }

    public List<Medico> getMedicos() { return Collections.unmodifiableList(medicos); }
    public List<Sala> getSalas() { return Collections.unmodifiableList(salas); }
}