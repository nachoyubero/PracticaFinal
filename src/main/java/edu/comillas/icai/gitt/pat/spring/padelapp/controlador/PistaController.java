package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.*;
import edu.comillas.icai.gitt.pat.spring.padelapp.servicios.ServicioAuth;
import edu.comillas.icai.gitt.pat.spring.padelapp.servicios.ServicioPistas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pistaPadel")
public class PistaController {

    @Autowired private ServicioPistas servicioPistas;
    @Autowired private ServicioAuth servicioAuth;

    // --- HEALTHCHECK ---
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API UP and Running");
    }

    // --- RUTAS DE USUARIOS ---
    @GetMapping("/users")
    public List<Usuario> obtenerUsuarios() {
        return servicioAuth.obtenerTodosUsuarios();
    }
    @GetMapping("/users/{id}")
    public Usuario obtenerUsuario(@PathVariable Integer id) {
        return servicioAuth.obtenerUsuarioPorId(id);
    }
    @PatchMapping("/users/{id}")
    public Usuario modificarUsuario(@PathVariable Integer id, @RequestBody Usuario datos) {
        return servicioAuth.modificarUsuario(id, datos);
    }

    // --- RUTAS DE PISTAS ---
    @PostMapping("/courts")
    @ResponseStatus(HttpStatus.CREATED)
    public Pista crearPista(@RequestBody Pista pista) {
        return servicioPistas.crearPista(pista);
    }
    @GetMapping("/courts")
    public List<Pista> listarPistas(@RequestParam(required = false) Boolean active) {
        return servicioPistas.listarPistas(active);
    }
    @GetMapping("/courts/{id}")
    public Pista obtenerPista(@PathVariable Integer id) {
        return servicioPistas.obtenerPista(id);
    }
    @PatchMapping("/courts/{id}")
    public Pista modificarPista(@PathVariable Integer id, @RequestBody Pista datos) {
        return servicioPistas.modificarPista(id, datos);
    }
    @DeleteMapping("/courts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPista(@PathVariable Integer id) {
        servicioPistas.eliminarPista(id);
    }

    // --- RUTAS DE DISPONIBILIDAD ---
    @GetMapping("/availability")
    public ResponseEntity<List<Disponibilidad>> consultarDisponibilidad(
            @RequestParam LocalDate date,
            @RequestParam(required = false) Integer courtId) {
        if (courtId != null) {
            return ResponseEntity.ok(List.of(servicioPistas.consultarDisponibilidadPistaId(courtId, date)));
        }
        return ResponseEntity.ok(servicioPistas.consultarDisponibilidadGlobal(date));
    }

    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<Disponibilidad> consultarDisponibilidadPorId(
            @PathVariable Integer courtId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(servicioPistas.consultarDisponibilidadPistaId(courtId, date));
    }

    // --- RUTAS DE RESERVAS ---
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crearReserva(@RequestBody Reserva reserva) {
        return servicioPistas.crearReserva(reserva);
    }

    @GetMapping("/reservations")
    public List<Reserva> misReservas(
            @RequestParam Integer userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return servicioPistas.obtenerMisReservas(userId, from, to);
    }

    @GetMapping("/reservations/{reservationId}")
    public Reserva obtenerReserva(@PathVariable Integer reservationId, @RequestParam Integer userId) {
        return servicioPistas.obtenerReserva(reservationId, userId);
    }

    @PatchMapping("/reservations/{reservationId}")
    public Reserva reprogramarReserva(
            @PathVariable Integer reservationId,
            @RequestBody Reserva nuevosDatos,
            @RequestParam Integer userId) {
        return servicioPistas.modificarReserva(reservationId, nuevosDatos, userId);
    }

    @DeleteMapping("/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarReserva(@PathVariable Integer reservationId, @RequestParam Integer userId) {
        servicioPistas.cancelarReserva(reservationId, userId);
    }

    // --- RUTAS DE ADMIN ---
    @GetMapping("/admin/reservations")
    public List<Reserva> obtenerReservasAdmin(
            @RequestParam Integer adminId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer courtId,
            @RequestParam(required = false) Integer userId) {
        return servicioPistas.obtenerReservasAdmin(adminId, date, courtId, userId);
    }
}