package org.example.servicios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entidades.EspecialidadMedica;
import org.example.entidades.Medico;

import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MedicoService {

    private final EntityManager em;

    public MedicoService(EntityManager em) {
        this.em = em;
    }

    /**
     * Retorna un mapa con la cantidad de médicos por especialidad.
     */
    public Map<EspecialidadMedica, Long> contarMedicosPorEspecialidad() {
        Map<EspecialidadMedica, Long> resultado = new EnumMap<>(EspecialidadMedica.class);

        for (EspecialidadMedica esp : EspecialidadMedica.values()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(m) FROM Medico m WHERE m.especialidad = :esp",
                    Long.class
            );
            query.setParameter("esp", esp);
            Long count = query.getSingleResult();
            if (count > 0) {
                resultado.put(esp, count);
            }
        }

        return resultado;
    }

    /**
     * Opcional: Exportar los datos a CSV
     */
    public void exportarEstadisticasCSV(Map<EspecialidadMedica, Long> datos, String archivoPath) throws Exception {
        try (PrintWriter pw = new PrintWriter(archivoPath)) {
            pw.println("Especialidad,Cantidad");
            for (Map.Entry<EspecialidadMedica, Long> entry : datos.entrySet()) {
                pw.println(entry.getKey().name() + "," + entry.getValue());
            }
        }
    }
}