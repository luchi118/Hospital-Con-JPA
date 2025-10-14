package org.example.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Matricula {
    @Column(name = "numero_matricula", nullable = false, unique = true)
    private String numero;

    protected Matricula() { } // JPA

    public Matricula(String numero) {
        if (numero == null || !numero.matches("MP-\\d{4,6}")) {
            throw new IllegalArgumentException("Formato de matrícula inválido (MP-XXXX).");
        }
        this.numero = numero;
    }

    @Override
    public String toString() { return numero; }
}