package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Definimos el controlador REST para gestionar pistas y usuarios
@RestController
@RequestMapping("/pistaPadel")
public class PistaController {
    //Creamos hashmaps al no tener persistencia
    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<Integer, Pista> pistas = new HashMap<>();

    public PistaController() {
        Rol rolAdmin = new Rol(1, NombreRol.ADMIN, "Administrador del sistema");
        Rol rolUser = new Rol(2, NombreRol.USER, "Jugador normal");
        //Declaramos los roles y usuarios de prueba
        usuarios.put(1, new Usuario(1, "Pepe", "admin123", "García", true, LocalDateTime.now(), "600111222", rolAdmin, "admin@test.com"));
        usuarios.put(2, new Usuario(2, "Laura", "laura123", "López", true, LocalDateTime.now(), "600333444", rolUser, "laura@test.com"));
    }
    //Endpoint para crear una nueva pista
    @PostMapping("/courts")
    public ResponseEntity<Pista> crearPista(@RequestBody Pista pista) {
        pistas.put(pista.idPista(), pista);
        return ResponseEntity.status(HttpStatus.CREATED).body(pista);
    }
    //Endpoint para obtener todos los usuarios
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> obtenerUsuarios() {
        return ResponseEntity.ok(new ArrayList<>(usuarios.values()));
    }
    //Endpoint para obtener un usuario por su ID
    @GetMapping("/users/{idUsuario}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable int idUsuario) {
        if (usuarios.containsKey(idUsuario)) {
            return ResponseEntity.ok(usuarios.get(idUsuario));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    //Endpoint para modificar parcialmente un usuario
    @PatchMapping("/users/{idUsuario}")
    public ResponseEntity<Usuario> modificarUsuario(@PathVariable int idUsuario, @RequestBody Usuario datosNuevos) {
        if (!usuarios.containsKey(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario con ID " + idUsuario + " no existe.");
        }

        Usuario usuarioAntiguo = usuarios.get(idUsuario);

        if (datosNuevos.email() != null && !datosNuevos.email().equals(usuarioAntiguo.email())) {
            boolean emailOcupado = usuarios.values().stream().anyMatch(u -> u.email().equals(datosNuevos.email()));
            if (emailOcupado) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email " + datosNuevos.email() + " ya está en uso.");
            }
        }

        if (datosNuevos.email() != null && !datosNuevos.email().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El formato del email no es válido.");
        }

        Usuario usuarioActualizado = new Usuario(
                idUsuario,
                datosNuevos.nombre() != null ? datosNuevos.nombre() : usuarioAntiguo.nombre(),
                datosNuevos.password() != null ? datosNuevos.password() : usuarioAntiguo.password(),
                datosNuevos.apellidos() != null ? datosNuevos.apellidos() : usuarioAntiguo.apellidos(),
                datosNuevos.activo() != null ? datosNuevos.activo() : usuarioAntiguo.activo(),
                usuarioAntiguo.fechaAlta(),
                datosNuevos.telefono() != null ? datosNuevos.telefono() : usuarioAntiguo.telefono(),
                datosNuevos.rol() != null ? datosNuevos.rol() : usuarioAntiguo.rol(),
                datosNuevos.email() != null ? datosNuevos.email() : usuarioAntiguo.email()
        );

        usuarios.put(idUsuario, usuarioActualizado);
        return ResponseEntity.ok(usuarioActualizado);
    }
}