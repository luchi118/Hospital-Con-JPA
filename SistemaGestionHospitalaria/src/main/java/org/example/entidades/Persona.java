package org.example.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class Persona {

    @Column(nullable = false, length = 100)
    protected String nombre;

    @Column(nullable = false, length = 100)
    protected String apellido;

    @Column(nullable = false, unique = true, length = 8)
    protected String dni;

    @Column(name = "fecha_nacimiento", nullable = false)
    protected LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sangre", nullable = false, length = 15)
    protected TipoSangre tipoSangre;

    protected Persona(PersonaBuilder<?, ?> builder) {
        // Validar y asignar nombre
        this.nombre = validarString(builder.nombre, "El nombre no puede ser nulo ni vacío");
        // Validar y asignar apellido
        this.apellido = validarString(builder.apellido, "El apellido no puede ser nulo ni vacío");
        // Validar formato de DNI (7-8 dígitos)
        this.dni = validarDni(builder.dni);
        // Validar que fecha de nacimiento no sea nula
        this.fechaNacimiento = Objects.requireNonNull(builder.fechaNacimiento, "La fecha de nacimiento no puede ser nula");
        // Validar que tipo de sangre no sea nulo
        this.tipoSangre = Objects.requireNonNull(builder.tipoSangre, "El tipo de sangre no puede ser nulo");
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getEdad() {
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
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

    private String validarDni(String dni) {
        // Validar que no sea null
        Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        // Validar formato: 7 u 8 dígitos numéricos
        if (!dni.matches("\\d{7,8}")) {
            throw new IllegalArgumentException("El DNI debe tener 7 u 8 dígitos");
        }
        return dni;
    }

}
