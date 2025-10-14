package org.example.entidades;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "medicos")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Medico extends Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Matricula matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EspecialidadMedica especialidad;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @OneToMany(mappedBy = "medico",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private List<Cita> citas;

    protected Medico(MedicoBuilder<?, ?> b) {
        super(b);
        this.citas = new ArrayList<>();
        this.especialidad = Objects.requireNonNull(b.especialidad, "Especialidad no puede ser nula");

        if (b.matricula != null) {
            this.matricula = new Matricula(b.matricula.getNumero());
        } else if (b.numeroMatricula != null) {
            this.matricula = new Matricula(b.numeroMatricula);
        }
    }

    @Transient
    private String numeroMatricula;

    public List<Cita> getCitas() { return Collections.unmodifiableList(citas); }

    public void addCita(Cita cita) {
        this.citas.add(cita);
    }

    public void liberarCita(Cita cita) {
        if (cita != null) {
            citas.remove(cita);
        }
    }
}