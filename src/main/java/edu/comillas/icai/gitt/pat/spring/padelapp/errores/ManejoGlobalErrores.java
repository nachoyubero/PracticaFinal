package edu.comillas.icai.gitt.pat.spring.padelapp.errores;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.ModeloErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ManejoGlobalErrores {

    // 1. Errores manuales (ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarErroresEstado(ResponseStatusException ex, HttpServletRequest request) {

        // Limpiamos la URI para evitar XSS
        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());

        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                ex.getReason(),
                ex.getStatusCode().value(),
                rutaSegura, // Usamos la ruta limpia
                LocalDateTime.now()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // 2. Errores JSON mal formado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarJsonMalFormado(HttpMessageNotReadableException ex, HttpServletRequest request) {

        System.out.println("ERROR JSON CAPTURADO: " + ex.getMessage());
        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());

        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                "El formato del JSON enviado no es válido. Revisa tipos de datos y comas.",
                HttpStatus.BAD_REQUEST.value(),
                rutaSegura,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 3. Errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Datos inválidos");

        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());

        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                mensaje,
                HttpStatus.BAD_REQUEST.value(),
                rutaSegura,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 4. Error genérico 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarErrorGenerico(Exception ex, HttpServletRequest request) {

        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());

        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                "Error interno del servidor: " + ex.getMessage(), // OJO: En producción aquí pondrías un mensaje genérico
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                rutaSegura,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}