package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@ToString(exclude = {"hospital", "medicos", "salas"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name="departamento_tabla")
public class Departamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDepartamento;
    @Column(name = "nombre")
    private String nombre;
    @Column(name="especialidad")
    private EspecialidadMedica especialidad;

    @ManyToOne
    private Hospital hospital;
    @OneToMany(mappedBy="departamento")
    private final List<Medico> medicos = new ArrayList<>();
    @OneToMany(mappedBy="departamento")
    private final List<Sala> salas = new ArrayList<>();

    @Builder
    protected Departamento(String nombre, EspecialidadMedica especialidad) {
        this.nombre = validarString(nombre, "El nombre del departamento no puede ser nulo ni vacío");
        this.especialidad = Objects.requireNonNull(especialidad, "La especialidad no puede ser nula");
    }

    public void setHospital(Hospital hospital) {
        if (this.hospital != hospital) {
            if (this.hospital != null) {
                this.hospital.getInternalDepartamentos().remove(this);
            }
            this.hospital = hospital;
            if (hospital != null) {
                hospital.getInternalDepartamentos().add(this);
            }
        }
    }

    public void agregarMedico(Medico medico) {
        if (medico != null && !medicos.contains(medico)) {
            if(!medico.getEspecialidad().equals(this.especialidad)) {
                throw new IllegalArgumentException("Especialidad Incompatible");
            }
            medicos.add(medico);
            medico.setDepartamento(this);
        }
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

    public List<Medico> getMedicos() {
        return Collections.unmodifiableList(medicos);
    }

    public List<Sala> getSalas() {
        return Collections.unmodifiableList(salas);
    }


    private String validarString(String valor, String mensajeError) {
        Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }
}
