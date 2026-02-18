package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

public record MeResponse(
        Integer idUsuario,
        String nombre,
        String apellidos,
        String email,
        String telefono,
        String rol
) {}
