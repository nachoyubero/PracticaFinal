package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;

public record Rol(
        Integer idRol,
        NombreRol nombreRol,
        String descripcion
) {}