package org.example.Servicio;

import jakarta.persistence.EntityManager;
import org.example.entidades.EstadoCita;
import org.example.entidades.Hospital;

import java.util.Optional;

public class HospitalService {

    private final EntityManager em;

    public HospitalService( EntityManager em) {
        this.em = em;
    }

    public void mostrarInformacionHospital(Long id) {
        consultarHospitalPorId(id).ifPresentOrElse(
                hospital -> {
                    System.out.println(hospital.getNombre());
                    System.out.println("Dirección: " + hospital.getDireccion());
                    System.out.println("Teléfono: " + hospital.getTelefono());
                    System.out.println("Departamentos: " + hospital.getDepartamentos().toString());
                    System.out.println("Pacientes: " + hospital.getPacientes().toString());
                },() -> System.out.println("Hospital no encontrado")
        );
    }


    public Optional<Hospital> consultarHospitalPorId(Long id) {
        return em.createQuery("SELECT h FROM Hospital h WHERE h.id = :id", Hospital.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }


    public void generarResumenRecursos() {
        Long totalSalas = em.createQuery("SELECT COUNT(s) FROM Sala s", Long.class)
                .getSingleResult();

        Long totalPacientes = em.createQuery("SELECT COUNT(p) FROM Paciente p", Long.class)
                .getSingleResult();

        Long totalMedicos = em.createQuery("SELECT COUNT(m) FROM Medico m", Long.class)
                .getSingleResult();

        Long totalCitasProgramadas = em.createQuery(
                        "SELECT COUNT(c) FROM Cita c WHERE c.estado = :estado", Long.class)
                .setParameter("estado", EstadoCita.PROGRAMADA)
                .getSingleResult();

        // Mostrar en consola estilo dashboard
        System.out.println("===== DASHBOARD DE RECURSOS DEL HOSPITAL =====");
        System.out.println("Total de salas disponibles: " + totalSalas);
        System.out.println("Total de pacientes registrados: " + totalPacientes);
        System.out.println("Total de médicos activos: " + totalMedicos);
        System.out.println("Total de citas programadas: " + totalCitasProgramadas);
        System.out.println("==============================================");
    }
}
