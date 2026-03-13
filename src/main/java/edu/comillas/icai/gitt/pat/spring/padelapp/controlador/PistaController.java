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

    // --- RUTAS DE RESERVAS Y DISPONIBILIDAD ---
    @GetMapping("/availability")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam LocalDate date,
            @RequestParam(required = false) Integer courtId) {
        return ResponseEntity.ok(servicioPistas.consultarDisponibilidad(date, courtId));
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crearReserva(@RequestBody Reserva reserva) {
        return servicioPistas.crearReserva(reserva);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarReserva(@PathVariable Integer id, @RequestParam Integer userId) {
        servicioPistas.cancelarReserva(id, userId);
    }
}