package edu.comillas.icai.gitt.pat.spring.padelapp.errores;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.ModeloErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ManejoGlobalErrores {

    // Esta clase va a moldear todas las respuestas de error que se lancen en el proyecto, para que tengan un formato uniforme y legible.
    // Este captura los 404, 409, 400 que lancemos con ResponseStatusException en el controlador
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarErroresEstado(ResponseStatusException ex, HttpServletRequest request) {

        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                ex.getReason(),
                ex.getStatusCode().value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // 2. Maneja errores de JSON mal hechos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarJsonMalFormado(HttpMessageNotReadableException ex, HttpServletRequest request) {

        System.out.println("ERROR JSON CAPTURADO: " + ex.getMessage());
        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                "El formato del JSON enviado no es válido. Revisa tipos de datos y comas.",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Maneja error 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarErrorGenerico(Exception ex, HttpServletRequest request) {

        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                "Error interno del servidor: " + ex.getMessage(), // En producción, pon un mensaje genérico
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}