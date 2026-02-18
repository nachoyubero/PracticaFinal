package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final Map<Integer, Reserva> reservas = new HashMap<>();


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

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<String> cancelarReserva(@PathVariable Integer reservationId) {

        Reserva reserva = reservas.get(reservationId);

        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Comprobar permisos (403)
        int userID = reserva.idUsuario();
        NombreRol rolex = usuarios.get(userID).rol().nombreRol();
        if (rolex != NombreRol.ADMIN){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Comprobar política
        LocalDateTime fechaHoraPartido = LocalDateTime.of(reserva.fechaReserva(), reserva.horaInicio());
        if (LocalDateTime.now().plusHours(24).isAfter(fechaHoraPartido)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede cancelar con menos de 24h de antelación"); // Error 409
        }

        // Borrar la reserva de tu sistema
        reservas.remove(reservationId);
        // Éxito (204)
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<?> modificarReserva(
            @PathVariable Integer reservationId,
            @RequestBody Reserva nuevosDatos
    ) {

        // Buscar la reserva actual
        Reserva reservaActual = reservas.get(reservationId);
        if (reservaActual == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
        }

        // Comprobar permisos
        int userID = reservaActual.idUsuario();
        NombreRol rolex = usuarios.get(userID).rol().nombreRol();
        if (rolex != NombreRol.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
        }

        LocalDate nuevaFecha = nuevosDatos.fechaReserva() != null ? nuevosDatos.fechaReserva() : reservaActual.fechaReserva();
        LocalTime nuevaHora = nuevosDatos.horaInicio() != null ? nuevosDatos.horaInicio() : reservaActual.horaInicio();
        Integer nuevaDuracion = nuevosDatos.duracionMinutos() != null ? nuevosDatos.duracionMinutos() : reservaActual.duracionMinutos();
        Integer nuevaPista = nuevosDatos.idPista() != null ? nuevosDatos.idPista() : reservaActual.idPista();

        // Validación
        LocalDateTime nuevaFechaHora = LocalDateTime.of(nuevaFecha, nuevaHora);
        if (nuevaFechaHora.isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La nueva fecha es inválida (pasada)"); // 400
        }

        // Conflicto (409)
        boolean pistaOcupada = reservas.values().stream()
                .filter(r -> !r.idReserva().equals(reservationId))
                .filter(r -> r.idPista().equals(nuevaPista))       // Misma pista
                .filter(r -> r.fechaReserva().equals(nuevaFecha))  // Mismo día
                .anyMatch(r -> {
                    LocalTime finExistente = r.getHoraFin();
                    LocalTime finNuevo = nuevaHora.plusMinutes(nuevaDuracion);

                    return nuevaHora.isBefore(finExistente) && finNuevo.isAfter(r.horaInicio());
                });

        if (pistaOcupada) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nuevo horario ya está ocupado"); // 409
        }

        // Actualizar y guardar (200)
        Reserva reservaActualizada = new Reserva(
                reservaActual.idReserva(),
                reservaActual.idUsuario(),
                nuevaPista,
                nuevosDatos.estado() != null ? nuevosDatos.estado() : reservaActual.estado(),
                nuevaFecha,
                nuevaHora,
                reservaActual.fechaCreacion(),
                nuevaDuracion
        );

        reservas.put(reservationId, reservaActualizada);

        return ResponseEntity.ok(reservaActualizada);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<?> obtenerReservasAdmin(
            @RequestParam Integer adminId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer courtId,
            @RequestParam(required = false) Integer userId
    ) {

        // Autenticación
        Usuario usuarioPeticion = usuarios.get(adminId);
        if (usuarioPeticion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado"); // 401
        }

        // Autorización
        if (usuarioPeticion.rol().nombreRol() != NombreRol.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Se requiere rol ADMIN"); // 403
        }

        List<Reserva> reservasFiltradas = reservas.values().stream()
                .filter(r -> date == null || r.fechaReserva().equals(date))
                .filter(r -> courtId == null || r.idPista().equals(courtId))
                .filter(r -> userId == null || r.idUsuario().equals(userId))
                .toList();

        // Éxito (200)
        return ResponseEntity.ok(reservasFiltradas);
    }

    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("La API de Pádel está funcionando correctamente");
    }

}