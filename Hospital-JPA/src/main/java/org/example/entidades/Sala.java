package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "salas", uniqueConstraints = @UniqueConstraint(columnNames = {"numero"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private String tipo;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @OneToMany(mappedBy = "sala",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private List<Cita> citas = new ArrayList<>();

    @Builder
    protected Sala(String numero, String tipo) {
        this.numero = Objects.requireNonNull(numero, "Numero no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "Tipo no puede ser nulo");
    }

    public void agregarCita(Cita c) {
        Objects.requireNonNull(c, "Cita no puede ser null");
        c.getSala();
        citas.add(c);
    }

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