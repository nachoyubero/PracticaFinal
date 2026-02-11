package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import java.time.LocalDate;

public record Pista(
        int idPista,
        String nombre,
        String ubicacion,
        double precioHora,
        boolean activa,
        LocalDate fechaAlta //Aqui solo se sabe año-mes-día
) {}
