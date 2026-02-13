package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.Estado;

import java.time.LocalDate;
import java.time.LocalTime;

public record Reserva(
        Integer idReserva,
        Integer idUsuario,
        Integer idPista,
        Estado estado,
        LocalDate fechaReserva,
        LocalTime horaInicio,
        LocalDate fechaCreacion,
        Integer duracionMinutos
        // Falta hora fin que hay que calcular
) {}
