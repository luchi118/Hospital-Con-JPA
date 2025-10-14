package org.example.servicios;


import org.example.entidades.Cita;
import org.example.entidades.EstadoCita;

interface EstadoCitaStrategy {
    void avanzar(Cita cita);
    void cancelar(Cita cita);
    void marcarNoAsistio(Cita cita);
}

class ProgramadaStrategy implements EstadoCitaStrategy {

    @Override
    public void avanzar(Cita cita) {
        cita.setEstado(EstadoCita.EN_CURSO);
    }

    @Override
    public void cancelar(Cita cita) {
        cita.setEstado(EstadoCita.CANCELADA);
    }

    @Override
    public void marcarNoAsistio(Cita cita) {
        cita.setEstado(EstadoCita.NO_ASISTIO);
    }
}

 class EnCursoStrategy implements EstadoCitaStrategy {
    @Override
    public void avanzar(Cita cita) {
        cita.setEstado(EstadoCita.COMPLETADA);
    }
    @Override
    public void cancelar(Cita cita) {
        throw new IllegalStateException("No se puede cancelar mientras está en curso");
    }
    @Override
    public void marcarNoAsistio(Cita cita) {
        throw new IllegalStateException("No se puede marcar no asistencia mientras está en curso");
    }
}



public class EstadoCitaFactory {

    public static EstadoCitaStrategy getStrategy(Cita cita) {
        return switch(cita.getEstado()) {
            case PROGRAMADA -> new ProgramadaStrategy();
            case EN_CURSO -> new EnCursoStrategy();
            case COMPLETADA -> throw new IllegalStateException("La cita ya está completada");
            case CANCELADA -> throw new IllegalStateException("La cita ya está cancelada");
            case NO_ASISTIO -> throw new IllegalStateException("La cita ya fue marcada como no asistida");
        };
    }
}



// Las demás estrategias (COMPLETADA, CANCELADA, NO_ASISTIO) pueden lanzar excepción al intentar cambiar
