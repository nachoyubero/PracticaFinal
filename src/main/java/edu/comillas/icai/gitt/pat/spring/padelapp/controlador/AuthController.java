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

@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<String, Integer> sesiones = new HashMap<>();
    private final BCryptPasswordEncoder encoder;

    public AuthController(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest req) {

        String emailNorm = req.email().trim().toLowerCase();

        boolean emailExiste = usuarios.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(emailNorm));

        if (emailExiste) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso.");
        }

        int nuevoId = usuarios.keySet().stream().mapToInt(x -> x).max().orElse(0) + 1;

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

        usuarios.put(nuevoId, nuevo);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {

        String emailNorm = req.email().trim().toLowerCase();

        Usuario usuario = usuarios.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(emailNorm))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!encoder.matches(req.password(), usuario.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String token = UUID.randomUUID().toString();
        sesiones.put(token, usuario.idUsuario());

        return new LoginResponse(token);
    }


    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = extraerToken(authorization);

        if (token == null || !sesiones.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        sesiones.remove(token);
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

        if (token == null || !sesiones.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        Integer userId = sesiones.get(token);
        Usuario usuario = usuarios.get(userId);

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

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

