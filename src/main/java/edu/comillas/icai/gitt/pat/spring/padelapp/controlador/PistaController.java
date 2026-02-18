package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.Estado;
import edu.comillas.icai.gitt.pat.spring.padelapp.clases.FranjaHoraria;
import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
    //GET -> Listar pistas
    @GetMapping("/courts")
    public List<Pista> listarPistas(@RequestParam(required = false) Boolean active) {
        if (active != null) {
            // Filtramos el mapa de pistas según si están activas o no
            return pistas.values().stream()
                    .filter(p -> p.activa().equals(active))
                    .toList();
        }
        // Si no hay parámetro, devolvemos todas
        return new ArrayList<>(pistas.values());
    }

    //GET -> Detalle de una pista
    @GetMapping("/courts/{courtId}")
    public Pista obtenerDetallePista(@PathVariable Integer courtId) {
        if (!pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        }
        return pistas.get(courtId);
    }

    // PATCH -> Modificar pista
    @PatchMapping("/courts/{courtId}")
    public Pista modificarPista(@PathVariable Integer courtId, @RequestBody Pista datosNuevos) {
        if (!pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Pista existente = pistas.get(courtId);

        // Al ser un Record, creamos una copia nueva con los cambios (operador ternario)
        Pista actualizada = new Pista(
                courtId,
                datosNuevos.nombre() != null ? datosNuevos.nombre() : existente.nombre(),
                datosNuevos.ubicacion() != null ? datosNuevos.ubicacion() : existente.ubicacion(),
                datosNuevos.precioHora() != null ? datosNuevos.precioHora() : existente.precioHora(),
                datosNuevos.activa() != null ? datosNuevos.activa() : existente.activa(),
                existente.fechaAlta()
        );

        pistas.put(courtId, actualizada);
        return actualizada;
    }

    //DELETE -> Borrar con regla de conflicto (409)
    @DeleteMapping("/courts/{courtId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPista(@PathVariable Integer courtId) {
        if (!pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // REGLA DE NEGOCIO (Error 409):
        // Miramos en el mapa de 'reservas' si alguna pertenece a esta pista
        boolean tieneReservas = reservas.values().stream()
                .anyMatch(r -> r.idPista().equals(courtId));

        if (tieneReservas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: la pista tiene reservas asociadas.");
        }

        pistas.remove(courtId);
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

        // Buscar la reserva
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

    // comprobación de availability (de una o más pistas)
    @GetMapping("/availability")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam LocalDate date,
            @RequestParam(required = false) Integer courtId
    ) {
        if (date.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("La fecha debe de ser futura");
        }

        List<Disponibilidad> resultado = new ArrayList<>();

        // si se incluye la pista
        if (courtId != null) {
            if (pistas.containsKey(courtId)) {
                resultado.add(calcularDisponibilidadPista(pistas.get(courtId), date));
            } else {
                // Si la pista no existe tenemos dos opciones, 404 o devolver lista vacía
                // Nos convence más 404
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista solicitada no existe");
            }
        } else {
            // Todas las pistas activas
            // iteramos y añadimos disponibilidades
            pistas.values().stream()
                    .filter(Pista::activa)
                    .forEach(p -> resultado.add(calcularDisponibilidadPista(p, date)));
        }

        // devolvemos 200 con el resultado
        return ResponseEntity.ok(resultado);
    }

    // comprobación de availability
    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<?> consultarDisponibilidadPorId(
            @PathVariable Integer courtId,
            @RequestParam LocalDate date
    ) {
        if (!pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe.");
        }

        // Comprobar validez de la fecha
        if (date.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("No se puede consultar disponibilidad de fechas pasadas.");
        }

        // igual que en el anterior, devuelvo un ok con la disponibilidad (o falta de)
        return ResponseEntity.ok(calcularDisponibilidadPista(pistas.get(courtId), date));
    }

    // post de reserva
    // POST /pistaPadel/reservations
    // Usamos el propio record Reserva como entrada para simplificar
    @PostMapping("/reservations")
    public ResponseEntity<?> crearReserva(@RequestBody Reserva entrada) {

        // validamos que todos los datos estan puestos
        if (entrada.idUsuario() == null || entrada.idPista() == null || entrada.fechaReserva() == null ||
                entrada.horaInicio() == null || entrada.duracionMinutos() == null) {
            return ResponseEntity.badRequest().body("Faltan datos de la reserva");
        }

        // Comprobamos que la pista existe (404)
        if (!pistas.containsKey(entrada.idPista())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La pista solicitada no existe");
        }

        // Comprobamos que la fecha es futura (400)
        LocalDateTime inicioReserva = LocalDateTime.of(entrada.fechaReserva(), entrada.horaInicio());
        if (inicioReserva.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("La fecha debe de ser futura");
        }

        // Comprobamos que la pista está disponible (409)
        Pista pista = pistas.get(entrada.idPista());

        // Calculamos los huecos libres
        Disponibilidad disponibilidad = calcularDisponibilidadPista(pista, entrada.fechaReserva());

        LocalTime horaFinSolicitada = entrada.horaInicio().plusMinutes(entrada.duracionMinutos());

        // Comprobación de si la reserva entra
        boolean hayHueco = disponibilidad.franjasDisponibles().stream().anyMatch(franja -> {
            boolean empiezaBien = !entrada.horaInicio().isBefore(franja.getInicio());
            boolean terminaBien = !horaFinSolicitada.isAfter(franja.getFin());
            return empiezaBien && terminaBien;
        });

        // error 409
        if (!hayHueco) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No hay disponibilidad para el horario solicitado");
        }

        // Crear reserva (todo en orden, 201)
        int nuevoId = reservas.keySet().stream().mapToInt(k -> k).max().orElse(0) + 1;

        Reserva nuevaReserva = new Reserva(
                nuevoId,
                entrada.idUsuario(),
                entrada.idPista(),
                Estado.ACTIVA,
                entrada.fechaReserva(),
                entrada.horaInicio(),
                LocalDate.now(),
                entrada.duracionMinutos()
        );

        reservas.put(nuevoId, nuevaReserva);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
    }

    // funcion para comprobar disponibilidad
    private Disponibilidad calcularDisponibilidadPista(Pista pista, LocalDate fecha) {
        // Tomamos que abrimos a las 9 y cerramos a las 22
        LocalTime horaApertura = LocalTime.of(9, 0);
        LocalTime horaCierre = LocalTime.of(22, 0);

        List<FranjaHoraria> franjasLibres = new ArrayList<>();

        // Filtramos reservas de la pista y la fecha, quitando las canceladas
        // las ordenamos por orden de hora de inicio
        List<Reserva> reservasOrdenadas = reservas.values().stream()
                .filter(r -> r.idPista().equals(pista.idPista()))
                .filter(r -> r.fechaReserva().equals(fecha))
                .filter(r -> r.estado() != edu.comillas.icai.gitt.pat.spring.padelapp.clases.Estado.CANCELADA)
                .sorted((r1, r2) -> r1.horaInicio().compareTo(r2.horaInicio()))
                .toList();

        // iteramos desde la hora de apertura
        LocalTime ultimoFin = horaApertura;

        for (Reserva reserva : reservasOrdenadas) {
            LocalTime inicioReserva = reserva.horaInicio();
            LocalTime finReserva = reserva.getHoraFin();

            // Si hay espacio entre ultimoFin y la siguiente reserva
            if (ultimoFin.isBefore(inicioReserva)) {
                // se añade como hueco libre
                franjasLibres.add(new FranjaHoraria(ultimoFin, inicioReserva));
            }

            ultimoFin = finReserva;
        }

        // Comprobar hasta cierre
        if (ultimoFin.isBefore(horaCierre)) {
            franjasLibres.add(new FranjaHoraria(ultimoFin, horaCierre));
        }

        return new Disponibilidad(pista.idPista(), fecha, franjasLibres);
    }

}