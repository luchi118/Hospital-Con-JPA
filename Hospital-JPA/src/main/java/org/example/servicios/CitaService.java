package org.example.servicios;


import org.example.entidades.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CitaService {

    Cita programarCita(Paciente paciente, Medico medico, Sala sala,
                       LocalDateTime fechaHora, BigDecimal costo) throws CitaException;

    List<Cita> getCitasPorPaciente(Paciente paciente);


    List<Cita> getCitasPorMedico(Medico medico);


    List<Cita> getCitasPorSala(Sala sala);

}