package org.example.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@SuperBuilder

@Entity
@Table(name="médico_tabla")
public class Medico extends Persona{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedico;

    @Embedded
    private Matricula matricula;

    @Enumerated(EnumType.STRING)
    @Column(name="especialidad", nullable = false)
    private final EspecialidadMedica especialidad;

    @ManyToOne
    @JoinColumn(name= "departamento_id")
    private Departamento departamento;
    @OneToMany(mappedBy = "medico", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private final List<Cita> citas;


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

    public void setDepartamento(Departamento departamento) {
        if (this.departamento != departamento) {
            this.departamento = departamento;
        }
    }

    public void addCita(Cita cita) {
        this.citas.add(cita);
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(new ArrayList<>(citas));
    }


}