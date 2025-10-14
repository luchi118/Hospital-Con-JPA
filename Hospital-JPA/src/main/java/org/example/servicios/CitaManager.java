package org.example.servicios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.entidades.*;
import org.example.servicios.CitaService;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CitaManager implements CitaService {


    private final EntityManager em;

    public CitaManager(EntityManager em) {
        this.em = em;
    }

    private final List<Cita> citas = new ArrayList<>();

    private final Map<Paciente, List<Cita>> citasPorPaciente = new ConcurrentHashMap<>();

    private final Map<Medico, List<Cita>> citasPorMedico = new ConcurrentHashMap<>();

    private final Map<Sala, List<Cita>> citasPorSala = new ConcurrentHashMap<>();

    @Override
    public Cita programarCita(Paciente paciente, Medico medico, Sala sala,
                              LocalDateTime fechaHora, BigDecimal costo) throws CitaException {

        // Validación de restricciones básicas
        validarCita(fechaHora, costo);

        // Validación de disponibilidad del médico
        if (!esMedicoDisponible(medico, fechaHora)) {
            throw new CitaException("El médico no está disponible en la fecha y hora solicitadas.");
        }

        // Validación de disponibilidad de la sala
        if (!esSalaDisponible(sala, fechaHora)) {
            throw new CitaException("La sala no está disponible en la fecha y hora solicitadas.");
        }

        // Validación de compatibilidad de especialidades
        if (!medico.getEspecialidad().equals(sala.getDepartamento().getEspecialidad())) {
            throw new CitaException("La especialidad del médico no coincide con el departamento de la sala.");
        }

        // Crear la cita
        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .sala(sala)
                .fechaHora(fechaHora)
                .costo(costo)
                .build();
        citas.add(cita);

        // Actualizar índices
        actualizarIndicePaciente(paciente, cita);
        actualizarIndiceMedico(medico, cita);
        actualizarIndiceSala(sala, cita);

        // Actualizar relaciones bidireccionales en entidades
        paciente.addCita(cita);
        medico.addCita(cita);
        sala.addCita(cita);

        // Persistir
        em.persist(cita);
        return cita;
    }


    private void validarCita(LocalDateTime fechaHora, BigDecimal costo) throws CitaException {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new CitaException("No se puede programar una cita en el pasado.");
        }

        if (costo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CitaException("El costo debe ser mayor que cero.");
        }
    }

    private boolean esMedicoDisponible(Medico medico, LocalDateTime fechaHora) {
        List<Cita> citasExistentes = citasPorMedico.get(medico);
        if (citasExistentes != null) {
            for (Cita citaExistente : citasExistentes) {
                // Verificar si hay conflicto de horario (buffer de ~2 horas)
                if (Math.abs(citaExistente.getFechaHora().compareTo(fechaHora)) < 2) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean esSalaDisponible(Sala sala, LocalDateTime fechaHora) {
        List<Cita> citasExistentes = citasPorSala.get(sala);
        if (citasExistentes != null) {
            for (Cita citaExistente : citasExistentes) {
                // Verificar si hay conflicto de horario (buffer de ~2 horas)
                if (Math.abs(citaExistente.getFechaHora().compareTo(fechaHora)) < 2) {
                    return false;
                }
            }
        }
        return true;
    }


    private void actualizarIndicePaciente(Paciente paciente, Cita cita) {
        List<Cita> citasPaciente = citasPorPaciente.get(paciente);
        if (citasPaciente == null) {
            citasPaciente = new ArrayList<>();
            citasPorPaciente.put(paciente, citasPaciente);
        }
        citasPaciente.add(cita);
    }

    private void actualizarIndiceMedico(Medico medico, Cita cita) {
        List<Cita> citasMedico = citasPorMedico.get(medico);
        if (citasMedico == null) {
            citasMedico = new ArrayList<>();
            citasPorMedico.put(medico, citasMedico);
        }
        citasMedico.add(cita);
    }

    private void actualizarIndiceSala(Sala sala, Cita cita) {
        List<Cita> citasSala = citasPorSala.get(sala);
        if (citasSala == null) {
            citasSala = new ArrayList<>();
            citasPorSala.put(sala, citasSala);
        }
        citasSala.add(cita);
    }


    @Override
    public List<Cita> getCitasPorPaciente(Paciente paciente) {
        List<Cita> citasPaciente = citasPorPaciente.get(paciente);
        if (citasPaciente != null) {
            return Collections.unmodifiableList(citasPaciente);
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public List<Cita> getCitasPorMedico(Medico medico) {
        List<Cita> citasMedico = citasPorMedico.get(medico);
        if (citasMedico != null) {
            return Collections.unmodifiableList(citasMedico);
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public List<Cita> getCitasPorSala(Sala sala) {
        List<Cita> citasSala = citasPorSala.get(sala);
        if (citasSala != null) {
            return Collections.unmodifiableList(citasSala);
        } else {
            return Collections.emptyList();
        }
    }

    public void actualizarEstado(Long idCita, String accion) {
        Cita cita = em.find(Cita.class, idCita);
        if (cita == null) throw new IllegalArgumentException("Cita no encontrada");

        EstadoCitaStrategy strategy = EstadoCitaFactory.getStrategy(cita);

        switch (accion.toUpperCase()) {
            case "AVANZAR" -> strategy.avanzar(cita);
            case "CANCELAR" -> strategy.cancelar(cita);
            case "NO_ASISTIO" -> strategy.marcarNoAsistio(cita);
            default -> throw new IllegalArgumentException("Acción inválida: " + accion);
        }

        // Guardar cambios
        em.persist(cita);
    }


    public void cancelarCita(Long idCita, String motivo) {
        Cita cita = em.find(Cita.class, idCita);
        if (cita == null) throw new IllegalArgumentException("Cita no encontrada");

        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new IllegalStateException("Solo se pueden cancelar citas PROGRAMADAS");
        }

        // Cambiar estado a CANCELADA (usando tu Strategy)
        actualizarEstado(idCita, "CANCELAR");

        // Registrar motivo
        if (motivo != null && !motivo.isBlank()) {
            String obsActual = cita.getObservaciones() == null ? "" : cita.getObservaciones();
            cita.setObservaciones(obsActual + " | Cancelación: " + motivo);
        }

        // Liberar disponibilidad del médico y sala
        Medico medico = cita.getMedico();
        Sala sala = cita.getSala();

        if (medico != null) medico.liberarCita(cita);
        if (sala != null) sala.liberarCita(cita);

        // También actualizá tus índices en memoria si querés mantener consistencia:
        citasPorMedico.getOrDefault(medico, new ArrayList<>()).remove(cita);
        citasPorSala.getOrDefault(sala, new ArrayList<>()).remove(cita);

        // Persistir cambios
        em.merge(cita); // merge mejor que persist, porque la cita ya existe
    }



    public void reporteCitasPorEstado() {
        Long totalCitas = em.createQuery("SELECT COUNT(c) FROM Cita c", Long.class)
                .getSingleResult();

        System.out.println("Reporte de Citas por Estado:");
        System.out.println("---------------------------");

        for (EstadoCita estado : EstadoCita.values()) {
            Long count = em.createQuery(
                            "SELECT COUNT(c) FROM Cita c WHERE c.estado = :estado",
                            Long.class)
                    .setParameter("estado", estado)
                    .getSingleResult();

            if (count > 0) {
                double porcentaje = totalCitas > 0 ? (count * 100.0) / totalCitas : 0;
                System.out.printf("%-12s: %d (%.2f%%)%n", estado, count, porcentaje);
            }
        }
    }


    // ---------------------------
    // EXPORTAR CITAS A CSV
    // ---------------------------
    public void exportarCitasACSV(String pathArchivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(pathArchivo))) {
            // Encabezado
            bw.write("dniPaciente,dniMedico,numeroSala,fechaHora,costo,estado,observaciones");
            bw.newLine();

            for (Cita cita : citas) {
                bw.write(cita.toCsvString());
                bw.newLine();
            }
        }
    }

    // ---------------------------
    // IMPORTAR CITAS DESDE CSV
    // ---------------------------
    public void importarCitasDesdeCSV(String pathArchivo) throws IOException, CitaException {
        List<Cita> nuevasCitas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(pathArchivo))) {
            String linea = br.readLine(); // Saltear encabezado

            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split(",", -1);

                if (campos.length != 7) {
                    throw new CitaException("Formato inválido en CSV: " + linea);
                }

                String dniPaciente = campos[0];
                String dniMedico = campos[1];
                String numeroSala = campos[2];
                LocalDateTime fechaHora = LocalDateTime.parse(campos[3]);
                BigDecimal costo = new BigDecimal(campos[4]);
                EstadoCita estado = EstadoCita.valueOf(campos[5]);
                String observaciones = campos[6].replace(";", ",");

                // Buscar entidades en BD
                Paciente paciente = em.createQuery("SELECT p FROM Paciente p WHERE p.dni = :dni", Paciente.class)
                        .setParameter("dni", dniPaciente)
                        .getResultStream().findFirst()
                        .orElseThrow(() -> new CitaException("Paciente no encontrado: " + dniPaciente));

                Medico medico = em.createQuery("SELECT m FROM Medico m WHERE m.dni = :dni", Medico.class)
                        .setParameter("dni", dniMedico)
                        .getResultStream().findFirst()
                        .orElseThrow(() -> new CitaException("Medico no encontrado: " + dniMedico));

                Sala sala = em.createQuery("SELECT s FROM Sala s WHERE s.numero = :num", Sala.class)
                        .setParameter("num", numeroSala)
                        .getResultStream().findFirst()
                        .orElseThrow(() -> new CitaException("Sala no encontrada: " + numeroSala));

                Cita cita = Cita.builder()
                        .paciente(paciente)
                        .medico(medico)
                        .sala(sala)
                        .fechaHora(fechaHora)
                        .costo(costo)
                        .estado(estado)
                        .observaciones(observaciones)
                        .build();

                nuevasCitas.add(cita);
            }
        }

        // Limpiar citas existentes y reconstruir índices
        citas.clear();
        citasPorPaciente.clear();
        citasPorMedico.clear();
        citasPorSala.clear();

        for (Cita cita : nuevasCitas) {
            citas.add(cita);

            // Reconstruir índices
            citasPorPaciente.computeIfAbsent(cita.getPaciente(), k -> new ArrayList<>()).add(cita);
            citasPorMedico.computeIfAbsent(cita.getMedico(), k -> new ArrayList<>()).add(cita);
            citasPorSala.computeIfAbsent(cita.getSala(), k -> new ArrayList<>()).add(cita);

            // Persistir en BD
            em.persist(cita);
        }
    }

}
