package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.entidades.*;
import org.example.servicios.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
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


    }
    private static void inicializarDB( EntityManager em){
        // ===========================================================
        // HU1 – Registrar un nuevo hospital y sus departamentos
        // ===========================================================
        Hospital hospital = Hospital.builder()
                .nombre("Hospital Lagomayore")
                .direccion("Av. Bologne sur mer 123")
                .telefono("0261-1233212")
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

        // ===========================================================
        // HU3 – Crear una sala dentro de un departamento
        // ===========================================================
        Sala s1 = cardiologia.crearSala("CARD-101", "Consultorio");
        Sala s2 = pediatria.crearSala("PED-201", "Consultorio");
        Sala s3 = traumatologia.crearSala("TRA-301", "Quirófano");

        // ===========================================================
        // HU2 – Registrar un médico en un departamento
        // ===========================================================
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

        // ===========================================================
        // HU1 (continuación) – Registrar un nuevo paciente
        // ===========================================================
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

        // ===========================================================
        // HU6 – Registrar diagnósticos y tratamientos en la historia clínica
        // ===========================================================
        p1.getHistoriaClinica().agregarDiagnostico("Hipertensión arterial");
        p1.getHistoriaClinica().agregarTratamiento("Enalapril 10mg");
        p1.getHistoriaClinica().agregarAlergia("Lacteos");

        // Persistencia inicial (hospital, departamentos, médicos, pacientes, etc.)
        em.persist(hospital);

        // HS 17
        PacienteService ps = new PacienteService(em);
        System.out.println("===== Muestro paciente con historia clinica =====\n");
        ps.mostrarInformacionPaciente(p1.getId());







        // ===========================================================
        // HU4 – Programar una cita médica
        // ===========================================================
        CitaManager cm = new CitaManager(em);
        try {
            Cita cita1 = cm.programarCita(
                    p1,
                    m1,
                    s1,
                    LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                    new BigDecimal("1500.00")
            );

            Cita cita2 = cm.programarCita(
                    p1,
                    m2,
                    s2,
                    LocalDateTime.now().plusDays(2).withHour(11).withMinute(0),
                    new BigDecimal("1200.00")
            );

            // HS 19
            cm.actualizarEstado(cita2.getId(),"avanzar");

            //HS 21
            cm.cancelarCita(cita1.getId(),"test para cancelar cita");

            System.out.println("===== Muestro cita modificada =====\n");
            System.out.println(cita1.toCsvString());



        } catch (CitaException e) {
            em.getTransaction().rollback();
            System.err.println("Error programando cita: " + e.getMessage());
        }

        // ===========================================================
        // HU 22 23 24 – Consultar citas programadas
        // ===========================================================
        System.out.println("===== Muestro citas por medicos por salas por pacientes =====\n");
        List<Cita> citasmedico2 = cm.getCitasPorMedico(m2);
        List<Cita> citaspaciente2 = cm.getCitasPorPaciente(p1);
        List<Cita> citassalas2 = cm.getCitasPorSala(s2);
        System.out.println(citasmedico2);
        System.out.println(citaspaciente2);
        System.out.println(citassalas2);


        // ===========================================================
        // H25 – Generar Estadísticas por Especialidad
        // ===========================================================
        System.out.println("===== Muestro medicos por especialidad =====\n");
        MedicoService ms = new MedicoService(em);
        Map<EspecialidadMedica, Long> stats = ms.contarMedicosPorEspecialidad();
        stats.forEach((esp, cant) -> System.out.println(esp + ": " + cant));
        try{
        ms.exportarEstadisticasCSV(stats, "./medicos_por_especialidad.csv");
        }catch(Exception e){
            em.getTransaction().rollback();
            System.err.println("Error con estasdisticas medico: " + e.getMessage());
        }


        // ===========================================================
        // H26 – Generar Estadísticas citas
        // ===========================================================
        System.out.println("===== Muestro citas por estado reporrte =====\n");
        cm.reporteCitasPorEstado();
        System.out.println("SISTEMA EJECUTADO EXITOSAMENTE");

        // ===========================================================
        // H27 – Generar reporte alergias
        // ===========================================================
        System.out.println("===== Muestro reporte alergias =====\n");
        ps.generarReportePacientesConAlergias();


        // ===========================================================
        // H28 – Generar reporte de recursos hospital
        // ===========================================================
        System.out.println("===== Muestro reporte hospital recursos =====\n");
        HospitalService hs = new HospitalService(em);
        hs.generarResumenRecursos();


        // ===========================================================
        // H29 – exportar archivo csv
        // ===========================================================
        System.out.println("===== Export archivo csv =====\n");
        try{
        cm.exportarCitasACSV("C:\\Users\\Bruno\\Downloads\\DS\\jpa\\Hospital-JPA\\Hospital-JPA\\exportFile\\csvarchivo");
        }catch(Exception e){
            em.getTransaction().rollback();
            System.err.println("Error exportando el archivo csv: " + e.getMessage());
        }


    }
}