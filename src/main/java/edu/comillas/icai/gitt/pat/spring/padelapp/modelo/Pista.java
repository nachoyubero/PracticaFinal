package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import java.time.LocalDate;

public record Pista(
        Integer idPista,
        String nombre,
        String ubicacion,
        Double precioHora,
        Boolean activa,
        LocalDate fechaAlta //Aquí solo se sabe año-mes-día
) {}
