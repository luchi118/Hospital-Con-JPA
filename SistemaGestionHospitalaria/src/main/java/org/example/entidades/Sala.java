package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@ToString(exclude = {"citas"})

@Entity
@Table(name = "salas", uniqueConstraints = @UniqueConstraint(columnNames = {"numero"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sala{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSala;

    @Column(name="número", nullable = false)
    private String numero;

    @Column(name="tipo",nullable = false)
    private String tipo;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="departamento_id", nullable = false)
    private Departamento departamento;

    @OneToMany(mappedBy = "sala",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private List<Cita> citas = new ArrayList<>();

    @Builder
    protected Sala(String numero, String tipo, Departamento departamento) {
        this.numero = Objects.requireNonNull(numero, "Numero no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "Tipo no puede ser nulo");
        this.departamento=Objects.requireNonNull(departamento, "Departamento no puede ser nulo");
    }

    public void addCita(Cita c) {
        Objects.requireNonNull(c, "Cita no puede ser null");
        c.getSala();
        citas.add(c);
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(new ArrayList<>(citas));
    }

    private String validarString(String valor, String mensajeError) {
        Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

}
