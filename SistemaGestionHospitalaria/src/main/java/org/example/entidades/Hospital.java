package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@EqualsAndHashCode
@Builder
@Getter
@ToString(exclude = {"departamentos", "pacientes"})

@Entity
@Table(name="hospital_tabla")
public class Hospital {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHospital;

    @Column(name="nombre", nullable = false)
    private final String nombre;
    @Column(name="dirección", nullable = false)
    private final String direccion;
    @Column(name="teléfono", nullable = false, length = 20)
    private final String telefono;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "hospital")
    private final List<Departamento> departamentos = new ArrayList<>();
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "hospital")
    private final List<Paciente> pacientes = new ArrayList<>();

    public void agregarDepartamento(Departamento departamento) {
        if (departamento != null && !departamentos.contains(departamento)) {
            departamentos.add(departamento);
            departamento.setHospital(this);
        }
    }

    public void agregarPaciente(Paciente paciente) {
        if (paciente != null && !pacientes.contains(paciente)) {
            pacientes.add(paciente);
            paciente.setHospital(this);
        }
    }

    public List<Departamento> getDepartamentos() {
        return Collections.unmodifiableList(departamentos);
    }

    public List<Paciente> getPacientes() {
        return Collections.unmodifiableList(pacientes);
    }

    List<Departamento> getInternalDepartamentos() {
        return departamentos;
    }

    List<Paciente> getInternalPacientes() {
        return pacientes;
    }

    private String validarString(String valor, String mensajeError) {
        Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }
}

