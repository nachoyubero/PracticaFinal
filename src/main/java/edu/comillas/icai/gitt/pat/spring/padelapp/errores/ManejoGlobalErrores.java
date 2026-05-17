package edu.comillas.icai.gitt.pat.spring.padelapp.errores;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.ModeloErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import java.time.LocalDateTime;

@RestControllerAdvice
public class ManejoGlobalErrores {

    private static final Logger logger = LoggerFactory.getLogger(ManejoGlobalErrores.class);

    // 1. Errores manuales (ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarErroresEstado(ResponseStatusException ex, HttpServletRequest request) {
        logger.debug("Error de estado {}: {}", ex.getStatusCode().value(), ex.getReason());
        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());
        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                ex.getReason(),
                ex.getStatusCode().value(),
                rutaSegura,
                LocalDateTime.now()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // 2. Errores JSON mal formado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarJsonMalFormado(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.debug("Error JSON capturado: {}", ex.getMessage());
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
        logger.debug("Error de validación: {}", mensaje);
        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());
        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                mensaje,
                HttpStatus.BAD_REQUEST.value(),
                rutaSegura,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 4. Parámetro obligatorio falta
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarParametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.debug("Parámetro faltante: {}", ex.getParameterName());
        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());
        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                "Parámetro obligatorio faltante: " + ex.getParameterName(),
                HttpStatus.BAD_REQUEST.value(),
                rutaSegura,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 5. Error 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ModeloErrorRespuesta> manejarErrorGenerico(Exception ex, HttpServletRequest request) {
        logger.error("Error interno del servidor: {}", ex.getMessage());
        String rutaSegura = HtmlUtils.htmlEscape(request.getRequestURI());
        ModeloErrorRespuesta error = new ModeloErrorRespuesta(
                "Error interno del servidor",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                rutaSegura,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}