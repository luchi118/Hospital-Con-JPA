package org.example.servicios;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.entidades.Cita;
import org.example.entidades.HistoriaClinica;
import org.example.entidades.Paciente;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PacienteService {

    private final EntityManager em;

    public PacienteService( EntityManager em) {
        this.em = em;
    }

    public void guardar(Paciente paciente) {
        // 🔹 Crear historia si no existe
        if (paciente.getHistoriaClinica() == null) {
            HistoriaClinica historia = HistoriaClinica.crearParaPaciente(paciente);
            paciente.setHistoriaClinica(historia);
        }
        em.persist(paciente);
    }



    public Optional<Paciente> buscarPorId(Long id) {
        try {
            Paciente paciente = em.createQuery(
                            "SELECT p FROM Paciente p " +
                                    "LEFT JOIN FETCH p.historiaClinica " +
                                    "LEFT JOIN FETCH p.citas " +
                                    "WHERE p.id = :id", Paciente.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(paciente);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    //HS 12
    public void mostrarInformacionPaciente(Long idPaciente) {
        Optional<Paciente> pacienteOpt = buscarPorId(idPaciente);

        if (pacienteOpt.isEmpty()) {
            System.out.println("⚠️ No se encontró el paciente con ID " + idPaciente);
            return;
        }

        Paciente p = pacienteOpt.get();

        System.out.println("===== INFORMACIÓN DEL PACIENTE =====");
        System.out.println("Nombre: " + p.getNombre() + " " + p.getApellido());
        System.out.println("DNI: " + p.getDni());
        System.out.println("Edad: " + p.getEdad());
        System.out.println("Tipo de sangre: " + p.getTipoSangre());

        if (p.getHistoriaClinica() != null) {
            System.out.println("Historia Clínica ID: " + p.getHistoriaClinica().getId());
            System.out.println("Diagnóstico actual: " + p.getHistoriaClinica().getDiagnosticos());
            System.out.println("Alergias actual: " + p.getHistoriaClinica().getAlergias());
            System.out.println("Tratamientos actual: " + p.getHistoriaClinica().getTratamientos());
        }

        System.out.println("Citas programadas:");
        p.getCitas().stream()
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .forEach(c -> System.out.println("🕓 " + c.getFechaHora() + " - " + c.getObservaciones()));

        System.out.println("====================================");
    }


    public List<Paciente> obtenerPacientesConAlergias() {
        TypedQuery<Paciente> query = em.createQuery(
                "SELECT DISTINCT p FROM Paciente p " +
                        "JOIN p.historiaClinica h " +
                        "WHERE SIZE(h.alergias) > 0",
                Paciente.class
        );
        return query.getResultList();
    }

    public void generarReportePacientesConAlergias() {
        List<Paciente> pacientes = obtenerPacientesConAlergias();

        System.out.println("===== REPORTE DE PACIENTES CON ALERGIAS =====");
        if (pacientes.isEmpty()) {
            System.out.println("No hay pacientes con alergias registradas.");
            return;
        }

        for (Paciente p : pacientes) {
            System.out.println("Paciente: " + p.getNombre() + " " + p.getApellido() +
                    " | DNI: " + p.getDni() +
                    " | Cantidad de alergias: " + p.getHistoriaClinica().getAlergias().size());
            System.out.println("Alergias: " + String.join(", ", p.getHistoriaClinica().getAlergias()));
            System.out.println("--------------------------------------------");
        }
    }
}
