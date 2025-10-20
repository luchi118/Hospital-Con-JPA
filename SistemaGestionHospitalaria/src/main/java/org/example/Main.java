package org.example;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import jakarta.persistence.TypedQuery;
import org.example.entidades.*;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hospital-persistence-unit");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            inicializarDB(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
        // Consultar médicos por especialidad
        TypedQuery<Medico> query = em.createQuery(
                "SELECT m FROM Medico m WHERE m.especialidad = :esp",
                Medico.class
        );
        query.setParameter("esp", EspecialidadMedica.CARDIOLOGIA);
        List<Medico> cardiologos = query.getResultList();

        // Contar citas por estado
        Long citasCompletadas = em.createQuery(
                        "SELECT COUNT(c) FROM Cita c WHERE c.estado = :estado",
                        Long.class
                )
                .setParameter("estado", EstadoCita.COMPLETADA)
                .getSingleResult();

        // Obtener pacientes con alergias
        TypedQuery<Paciente> queryAlergicos = em.createQuery(
                "SELECT DISTINCT p FROM Paciente p " +
                        "JOIN p.historiaClinica h " +
                        "WHERE SIZE(h.alergias) > 0",
                Paciente.class
        );

    }
    private static void inicializarDB( EntityManager em){

        Hospital hospital = Hospital.builder()
                .nombre("Hospital Italiano")
                .direccion("Av. de Acceso Este 1070")
                .telefono("0810-333-3330")
                .build();

        Departamento cardiologia = Departamento.builder()
                .nombre("Cardiología")
                .especialidad(EspecialidadMedica.CARDIOLOGIA)
                .build();

        Departamento pediatria = Departamento.builder()
                .nombre("Pediatría")
                .especialidad(EspecialidadMedica.PEDIATRIA)
                .build();

        Departamento traumatologia = Departamento.builder()
                .nombre("Traumatología")
                .especialidad(EspecialidadMedica.TRAUMATOLOGIA)
                .build();

        hospital.agregarDepartamento(cardiologia);
        hospital.agregarDepartamento(pediatria);
        hospital.agregarDepartamento(traumatologia);


        Sala s1 = cardiologia.crearSala("CARD-101", "Consultorio");
        Sala s2 = pediatria.crearSala("PED-201", "Consultorio");
        Sala s3 = traumatologia.crearSala("TRA-301", "Quirófano");


        Medico m1 = Medico.builder()
                .nombre("Diego")
                .apellido("González")
                .dni("45423321")
                .fechaNacimiento(LocalDate.of(1975,5,15))
                .tipoSangre(TipoSangre.A_POSITIVO)
                .numeroMatricula("MP-12345")
                .especialidad(EspecialidadMedica.CARDIOLOGIA)
                .build();

        Medico m2 = Medico.builder()
                .nombre("Lucía")
                .apellido("Martínez")
                .dni("50323212")
                .fechaNacimiento(LocalDate.of(1982,3,2))
                .tipoSangre(TipoSangre.O_POSITIVO)
                .numeroMatricula("MP-54321")
                .especialidad(EspecialidadMedica.PEDIATRIA)
                .build();

        cardiologia.agregarMedico(m1);
        pediatria.agregarMedico(m2);

        Paciente p1 = Paciente.builder()
                .nombre("Caroilna")
                .apellido("López")
                .dni("54232123")
                .fechaNacimiento(LocalDate.of(1985,12,5))
                .tipoSangre(TipoSangre.A_POSITIVO)
                .telefono("011-1111")
                .direccion("La cumbre 321")
                .build();

        hospital.agregarPaciente(p1);

        p1.getHistoriaClinica().agregarDiagnostico("Hipertensión arterial");
        p1.getHistoriaClinica().agregarTratamiento("Enalapril 10mg");
        p1.getHistoriaClinica().agregarAlergia("Lacteos");

        em.persist(hospital);
    }


}