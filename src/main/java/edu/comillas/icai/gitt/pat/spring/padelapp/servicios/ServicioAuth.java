package edu.comillas.icai.gitt.pat.spring.padelapp.servicios;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.*;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServicioAuth {

    @Autowired private RepoUsuario repoUsuario;
    @Autowired private BCryptPasswordEncoder encoder;
    private final Map<String, Integer> sesionesActivas = new ConcurrentHashMap<>();

    public void register(RegisterRequest req) {
        String emailNorm = req.email().trim().toLowerCase();
        if (repoUsuario.findByEmail(emailNorm).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso.");
        }
        Rol rolUser = new Rol();
        rolUser.setIdRol(2);

        Usuario nuevo = new Usuario();
        nuevo.setNombre(req.nombre().trim());
        nuevo.setPassword(encoder.encode(req.password()));
        nuevo.setApellidos(req.apellidos().trim());
        nuevo.setActivo(true);
        nuevo.setFechaAlta(LocalDateTime.now());
        nuevo.setTelefono(req.telefono());
        nuevo.setRol(rolUser);
        nuevo.setEmail(emailNorm);
        repoUsuario.save(nuevo);
    }

    public LoginResponse login(LoginRequest req) {
        String emailNorm = req.email().trim().toLowerCase();
        Usuario usuario = repoUsuario.findByEmail(emailNorm)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!encoder.matches(req.password(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String token = UUID.randomUUID().toString();
        sesionesActivas.put(token, usuario.getIdUsuario());
        return new LoginResponse(token);
    }

    public void logout(String authorization) {
        String token = extraerToken(authorization);
        if (token == null || !sesionesActivas.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        sesionesActivas.remove(token);
    }

    public MeResponse me(String authorization) {
        Integer userId = obtenerIdUsuarioDesdeToken(authorization);
        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no existe"));
        return new MeResponse(usuario.getIdUsuario(), usuario.getNombre(), usuario.getApellidos(),
                usuario.getEmail(), usuario.getTelefono(), usuario.getRol().getNombreRol().name());
    }

    // Métodos de gestión de usuarios extraídos del PistaController
    public List<Usuario> obtenerTodosUsuarios() {
        return (List<Usuario>) repoUsuario.findAll();
    }

    public Usuario obtenerUsuarioPorId(Integer id) {
        return repoUsuario.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario modificarUsuario(Integer id, Usuario datos) {
        Usuario existente = obtenerUsuarioPorId(id);
        if (datos.getEmail() != null && !datos.getEmail().equals(existente.getEmail())) {
            if (repoUsuario.findByEmail(datos.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso.");
            }
            existente.setEmail(datos.getEmail());
        }
        if (datos.getNombre() != null) existente.setNombre(datos.getNombre());
        if (datos.getApellidos() != null) existente.setApellidos(datos.getApellidos());
        if (datos.getActivo() != null) existente.setActivo(datos.getActivo());
        return repoUsuario.save(existente);
    }

    // Métodos auxiliares
    private String extraerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return null;
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    public Integer obtenerIdUsuarioDesdeToken(String authHeader) {
        String token = extraerToken(authHeader);
        if (token == null || !sesionesActivas.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return sesionesActivas.get(token);
    }
}