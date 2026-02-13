package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import java.time.LocalDateTime;

// Esto define que datos va a recibir Postman cuando algo falle
public record ModeloErrorRespuesta(
        String error,
        int estado,
        String ruta,
        LocalDateTime fecha
) {}