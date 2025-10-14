package org.example.entidades;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "hospitales")
@Getter
@ToString(exclude = {"departamentos", "pacientes"})
@NoArgsConstructor
public class Hospital implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 300)
    private String direccion;

    @Column(nullable = false, length = 20)
    private String telefono;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Departamento> departamentos = new ArrayList<>();

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paciente> pacientes = new ArrayList<>();

    private Hospital(HospitalBuilder builder) {
        // Inicializar colecciones vacías para evitar NullPointerException
        this.departamentos = new ArrayList<>();
        this.pacientes = new ArrayList<>();

        // Validar y asignar campos obligatorios
        this.nombre = validarString(builder.nombre, "El nombre del hospital no puede ser nulo ni vacío");
        this.direccion = validarString(builder.direccion, "La dirección no puede ser nula ni vacía");
        this.telefono = validarString(builder.telefono, "El teléfono no puede ser nulo ni vacío");
    }


    public static class HospitalBuilder {
        private String nombre;
        private String direccion;
        private String telefono;

        public HospitalBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }


        public HospitalBuilder direccion(String direccion) {
            this.direccion = direccion;
            return this;
        }


        public HospitalBuilder telefono(String telefono) {
            this.telefono = telefono;
            return this;
        }


        public Hospital build() {
            return new Hospital(this);
        }
    }


    public static HospitalBuilder builder() {
        return new HospitalBuilder();
    }


    public void agregarDepartamento(Departamento departamento) {
        // Validar que el departamento no sea null y no esté duplicado
        if (departamento != null && !departamentos.contains(departamento)) {
            // Agregar a la colección interna
            departamentos.add(departamento);
            // Mantener consistencia bidireccional
            departamento.setHospital(this);
        }
    }
    //HS 11
    public void agregarPaciente(Paciente paciente) {
        // Validar que el paciente no sea null y no esté duplicado
        if (paciente != null && !pacientes.contains(paciente)) {
            // Agregar a la colección interna
            pacientes.add(paciente);
            // Mantener consistencia bidireccional
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
        // Validar que no sea null
        Objects.requireNonNull(valor, mensajeError);

        // Validar que no esté vacío (sin espacios)
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }

        return valor;
    }
}
