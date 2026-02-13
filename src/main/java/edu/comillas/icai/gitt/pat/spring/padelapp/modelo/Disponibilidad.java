package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.FranjaHoraria;

import java.time.LocalDate;
import java.util.List;

public record Disponibilidad(
        Integer idPista,            // [cite: 301]
        LocalDate fecha,            // [cite: 302]
        List<FranjaHoraria> franjasDisponibles // [cite: 303]
) {}