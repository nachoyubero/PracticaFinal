package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    private final Memoria memoria;
    private final BCryptPasswordEncoder encoder;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AuthController(Memoria memoria, BCryptPasswordEncoder encoder) {
        this.memoria = memoria;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest req) {

        logger.info("Intento de registro para email: {}", req.email());
        String emailNorm = req.email().trim().toLowerCase();

        boolean emailExiste = memoria.usuarios.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(emailNorm));

        if (emailExiste) {
            logger.warn("Login fallido para email: {}", emailNorm);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso.");
        }

        int nuevoId = memoria.usuarios.keySet().stream().mapToInt(x -> x).max().orElse(0) + 1;

        Rol rolUser = new Rol(2, NombreRol.USER, "Jugador normal");

        Usuario nuevo = new Usuario(
                nuevoId,
                req.nombre().trim(),
                encoder.encode(req.password()),
                req.apellidos().trim(),
                true,
                LocalDateTime.now(),
                req.telefono(),
                rolUser,
                emailNorm
        );

        memoria.usuarios.put(nuevoId, nuevo);
        logger.info("Usuario registrado correctamente con email: {}", emailNorm);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {

        logger.info("Intento de login para email: {}", req.email());
        String emailNorm = req.email().trim().toLowerCase();

        Usuario usuario = memoria.usuarios.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(emailNorm))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Login fallido: usuario no encontrado para {}", emailNorm);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
                });

        if (!encoder.matches(req.password(), usuario.password())) {
            logger.warn("Registro rechazado: email ya en uso {}", emailNorm);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String token = UUID.randomUUID().toString();
        memoria.sesiones.put(token, usuario.idUsuario());

        logger.info("Login correcto para usuario id: {}", usuario.idUsuario());
        return new LoginResponse(token);
    }


    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = extraerToken(authorization);

        if (token == null || !memoria.sesiones.containsKey(token)) {
            logger.warn("Logout rechazado: no autenticado (token ausente o inválido)");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        Integer userId = memoria.sesiones.get(token);

        memoria.sesiones.remove(token);

        logger.info("Logout realizado correctamente para usuario id: {}", userId);
    }


    private String extraerToken(String authorization) {
        if (authorization == null) return null;
        if (!authorization.startsWith("Bearer ")) return null;
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    @GetMapping("/me")
    public MeResponse me(@RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = extraerToken(authorization);

        if (token == null || !memoria.sesiones.containsKey(token)) {
            logger.warn("Acceso rechazado: no autenticado (token ausente o inválido");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        Integer userId = memoria.sesiones.get(token);
        Usuario usuario = memoria.usuarios.get(userId);

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        logger.debug("Consulta de datos personales para usuario id: {}", userId);

        return new MeResponse(
                usuario.idUsuario(),
                usuario.nombre(),
                usuario.apellidos(),
                usuario.email(),
                usuario.telefono(),
                usuario.rol().nombreRol().name()
        );
    }

}

