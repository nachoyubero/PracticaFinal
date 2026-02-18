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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

//Definimos el controlador REST para gestionar pistas y usuarios
@RestController
@RequestMapping("/pistaPadel")
public class PistaController {
    //Creamos hashmaps al no tener persistencia
    private final Memoria memoria;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public PistaController(Memoria memoria) {
        this.memoria = memoria;
        Rol rolAdmin = new Rol(1, NombreRol.ADMIN, "Administrador del sistema");
        Rol rolUser = new Rol(2, NombreRol.USER, "Jugador normal");
        memoria.usuarios.put(1, new Usuario(1, "Pepe", "admin123", "García", true, LocalDateTime.now(), "600111222", rolAdmin, "admin@test.com"));
        memoria.usuarios.put(2, new Usuario(2, "Laura", "laura123", "López", true, LocalDateTime.now(), "600333444", rolUser, "laura@test.com"));
        memoria.reservas.put(2, new Reserva( 2,
                2,
                1,
                Estado.ACTIVA,
                LocalDate.now().plusDays(1),
                LocalTime.of(17, 0),
                LocalDate.now(),
                90));
        memoria.pistas.put(1, new Pista(
                1,
                "Central 1",
                "Club Madrid Norte",
                25.0,
                true,
                LocalDate.now()
        ));

    }
    //Endpoint para crear una nueva pista
    @PostMapping("/courts")
    public ResponseEntity<Pista> crearPista(@RequestBody Pista pista) {
        memoria.pistas.put(pista.idPista(), pista);
        logger.info("Crear pista: id={}, nombre={}", pista.idPista(), pista.nombre());
        logger.info("Pista creada OK: id={}", pista.idPista());
        return ResponseEntity.status(HttpStatus.CREATED).body(pista);
    }
    //Endpoint para obtener todos los usuarios
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> obtenerUsuarios() {
        logger.debug("Listar usuarios");
        return ResponseEntity.ok(new ArrayList<>(memoria.usuarios.values()));
    }
    //Endpoint para obtener un usuario por su ID
    @GetMapping("/users/{idUsuario}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable int idUsuario) {
        logger.debug("Detalle usuario: id={}", idUsuario);
        if (memoria.usuarios.containsKey(idUsuario)) {
            return ResponseEntity.ok(memoria.usuarios.get(idUsuario));
        } else {
            logger.warn("Detalle usuario: no existe id={}", idUsuario);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    //GET -> Listar pistas
    @GetMapping("/courts")
    public List<Pista> listarPistas(@RequestParam(required = false) Boolean active) {
        logger.debug("Listar pistas (active={})", active);
        if (active != null) {
            // Filtramos el mapa de pistas según si están activas o no
            return memoria.pistas.values().stream()
                    .filter(p -> p.activa().equals(active))
                    .toList();
        }
        // Si no hay parámetro, devolvemos todas
        return new ArrayList<>(memoria.pistas.values());
    }

    //GET -> Detalle de una pista
    @GetMapping("/courts/{courtId}")
    public Pista obtenerDetallePista(@PathVariable Integer courtId) {
        logger.debug("Detalle pista: id={}", courtId);
        if (!memoria.pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        }
        return memoria.pistas.get(courtId);
    }

    // PATCH -> Modificar pista
    @PatchMapping("/courts/{courtId}")
    public Pista modificarPista(@PathVariable Integer courtId, @RequestBody Pista datosNuevos) {
        logger.info("Modificar pista: id={}", courtId);
        if (!memoria.pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Pista existente = memoria.pistas.get(courtId);

        // Al ser un Record, creamos una copia nueva con los cambios (operador ternario)
        Pista actualizada = new Pista(
                courtId,
                datosNuevos.nombre() != null ? datosNuevos.nombre() : existente.nombre(),
                datosNuevos.ubicacion() != null ? datosNuevos.ubicacion() : existente.ubicacion(),
                datosNuevos.precioHora() != null ? datosNuevos.precioHora() : existente.precioHora(),
                datosNuevos.activa() != null ? datosNuevos.activa() : existente.activa(),
                existente.fechaAlta()
        );

        memoria.pistas.put(courtId, actualizada);
        logger.info("Pista modificada OK: id={}", courtId);
        return actualizada;
    }

    //DELETE -> Borrar con regla de conflicto (409)
    @DeleteMapping("/courts/{courtId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPista(@PathVariable Integer courtId) {
        logger.info("Eliminar pista: id={}", courtId);
        if (!memoria.pistas.containsKey(courtId)) {
            logger.warn("Eliminar pista: pista no existe id={}", courtId);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // REGLA DE NEGOCIO (Error 409):
        // Miramos en el mapa de 'reservas' si alguna pertenece a esta pista
        boolean tieneReservas = memoria.reservas.values().stream()
                .anyMatch(r -> r.idPista().equals(courtId));

        if (tieneReservas) {
            logger.warn("Eliminar pista: conflicto, tiene reservas id={}", courtId);

            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: la pista tiene reservas asociadas.");
        }

        memoria.pistas.remove(courtId);
        logger.info("Pista eliminada OK: id={}", courtId);

    }

    //Endpoint para modificar parcialmente un usuario
    @PatchMapping("/users/{idUsuario}")
    public ResponseEntity<Usuario> modificarUsuario(@PathVariable int idUsuario, @RequestBody Usuario datosNuevos) {
        logger.info("Modificar usuario: id={}", idUsuario);
        if (!memoria.usuarios.containsKey(idUsuario)) {
            logger.warn("Modificar usuario: no existe id={}", idUsuario);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario con ID " + idUsuario + " no existe.");
        }

        Usuario usuarioAntiguo = memoria.usuarios.get(idUsuario);

        if (datosNuevos.email() != null && !datosNuevos.email().equals(usuarioAntiguo.email())) {
            boolean emailOcupado = memoria.usuarios.values().stream().anyMatch(u -> u.email().equals(datosNuevos.email()));
            if (emailOcupado) {
                logger.warn("Modificar usuario: email en uso id={} email={}", idUsuario, datosNuevos.email());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email " + datosNuevos.email() + " ya está en uso.");
            }
        }

        if (datosNuevos.email() != null && !datosNuevos.email().contains("@")) {
            logger.warn("Modificar usuario: email inválido id={} email={}", idUsuario, datosNuevos.email());
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

        memoria.usuarios.put(idUsuario, usuarioActualizado);
        logger.info("Usuario modificado OK: id={}", idUsuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<String> cancelarReserva(@PathVariable Integer reservationId, @RequestParam Integer userId) {
        logger.info("Cancelar reserva: reservationId={} solicitadoPorUserId={}", reservationId, userId);

        Reserva reserva = memoria.reservas.get(reservationId);

        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Usuario solicitante = memoria.usuarios.get(userId);
        if (solicitante == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario solicitante no identificado");
        }

        boolean esDueno = reserva.idUsuario().equals(userId);
        boolean esAdmin = solicitante.rol().nombreRol() == NombreRol.ADMIN;

        if (!esDueno && !esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LocalDateTime fechaHoraPartido = LocalDateTime.of(reserva.fechaReserva(), reserva.horaInicio());
        if (LocalDateTime.now().plusHours(24).isAfter(fechaHoraPartido)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede cancelar con menos de 24h de antelación");
        }

        memoria.reservas.remove(reservationId);
        logger.info("Reserva cancelada OK: reservationId={}", reservationId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<?> modificarReserva(
            @PathVariable Integer reservationId,
            @RequestBody Reserva nuevosDatos
    ) {
        logger.info("Modificar reserva: reservationId={}", reservationId);

        
        // Buscar la reserva actual
        Reserva reservaActual = memoria.reservas.get(reservationId);
        if (reservaActual == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
        }

        // Comprobar permisos
        int userID = reservaActual.idUsuario();
        NombreRol rolex = memoria.usuarios.get(userID).rol().nombreRol();
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
        boolean pistaOcupada =memoria.reservas.values().stream()
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

        memoria.reservas.put(reservationId, reservaActualizada);
        logger.info("Reserva modificada OK: reservationId={}", reservationId);

        return ResponseEntity.ok(reservaActualizada);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<?> obtenerReservasAdmin(
            @RequestParam Integer adminId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer courtId,
            @RequestParam(required = false) Integer userId
    ) {
        logger.info("Admin listar reservas: adminId={} date={} courtId={} userId={}", adminId, date, courtId, userId);

        // Autenticación
        Usuario usuarioPeticion = memoria.usuarios.get(adminId);
        if (usuarioPeticion == null) {
            logger.warn("Admin listar reservas: usuario no existe adminId={}", adminId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado"); // 401
        }

        // Autorización
        if (usuarioPeticion.rol().nombreRol() != NombreRol.ADMIN) {
            logger.warn("Admin listar reservas: forbidden adminId={}", adminId);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Se requiere rol ADMIN"); // 403
        }

        List<Reserva> reservasFiltradas = memoria.reservas.values().stream()
                .filter(r -> date == null || r.fechaReserva().equals(date))
                .filter(r -> courtId == null || r.idPista().equals(courtId))
                .filter(r -> userId == null || r.idUsuario().equals(userId))
                .toList();

        // Éxito (200)
        logger.info("Admin listar reservas OK: total={}", reservasFiltradas.size());
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
        logger.debug("Consultar disponibilidad: date={} courtId={}", date, courtId);
        if (date.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("La fecha debe de ser futura");
        }

        List<Disponibilidad> resultado = new ArrayList<>();

        // si se incluye la pista
        if (courtId != null) {
            if (memoria.pistas.containsKey(courtId)) {
                resultado.add(calcularDisponibilidadPista(memoria.pistas.get(courtId), date));
            } else {
                // Si la pista no existe tenemos dos opciones, 404 o devolver lista vacía
                // Nos convence más 404
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista solicitada no existe");
            }
        } else {
            // Todas las pistas activas
            // iteramos y añadimos disponibilidades
            memoria.pistas.values().stream()
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
        logger.debug("Consultar disponibilidad pista: courtId={} date={}", courtId, date);
        if (!memoria.pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe.");
        }

        // Comprobar validez de la fecha
        if (date.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("No se puede consultar disponibilidad de fechas pasadas.");
        }

        // igual que en el anterior, devuelvo un ok con la disponibilidad (o falta de)
        return ResponseEntity.ok(calcularDisponibilidadPista(memoria.pistas.get(courtId), date));
    }

    // post de reserva
    // POST /pistaPadel/reservations
    // Usamos el propio record Reserva como entrada para simplificar
    @PostMapping("/reservations")
    public ResponseEntity<?> crearReserva(@RequestBody Reserva entrada) {
        logger.info("Crear reserva: userId={} pistaId={} fecha={} hora={} duracion={}"
                ,entrada.idUsuario(), entrada.idPista(), entrada.fechaReserva(), entrada.horaInicio(), entrada.duracionMinutos());

        // validamos que todos los datos estan puestos
        if (entrada.idUsuario() == null || entrada.idPista() == null || entrada.fechaReserva() == null ||
                entrada.horaInicio() == null || entrada.duracionMinutos() == null) {
            return ResponseEntity.badRequest().body("Faltan datos de la reserva");
        }

        // Comprobamos que la pista existe (404)
        if (!memoria.pistas.containsKey(entrada.idPista())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La pista solicitada no existe");
        }

        // Comprobamos que la fecha es futura (400)
        LocalDateTime inicioReserva = LocalDateTime.of(entrada.fechaReserva(), entrada.horaInicio());
        if (inicioReserva.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("La fecha debe de ser futura");
        }

        // Comprobamos que la pista está disponible (409)
        Pista pista = memoria.pistas.get(entrada.idPista());

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

        // Crear reserva (en orden, 201)
        int nuevoId = memoria.reservas.keySet().stream().mapToInt(k -> k).max().orElse(0) + 1;

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

        memoria.reservas.put(nuevoId, nuevaReserva);
        logger.info("Reserva creada OK: id={}", nuevoId);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
    }

    // get reservas
    @GetMapping("/reservations")
    public ResponseEntity<?> misReservas(
            @RequestParam Integer userId, // Usuario
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        logger.debug("Mis reservas: userId={} from={} to={}", userId, from, to);
        // Validar usuario (401)
        if (!memoria.usuarios.containsKey(userId)) {
            logger.warn("Mis reservas: usuario no existe userId={}", userId);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no existe");
        }

        List<Reserva> listaReservas = memoria.reservas.values().stream()
                .filter(r -> r.idUsuario().equals(userId)) // Filtro principal: Solo las mías
                // Filtro de fecha 'from' (si existe)
                .filter(r -> from == null || !r.fechaReserva().isBefore(from))
                // Filtro de fecha 'to' (si existe)
                .filter(r -> to == null || !r.fechaReserva().isAfter(to))
                // Opcional: Ordenarlas por fecha y hora
                .sorted(Comparator.comparing(Reserva::fechaReserva).thenComparing(Reserva::horaInicio))
                .toList();

        return ResponseEntity.ok(listaReservas); // 200 OK
    }


    // Get reserva por ID
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> obtenerReserva(
            @PathVariable Integer reservationId,
            @RequestParam Integer userId // para comprobar si puede solicitar
    ) {
        logger.debug("Detalle reserva: reservationId={} solicitadoPorUserId={}", reservationId, userId);
        // Comprobar que existe (Error 404)
        Reserva reserva = memoria.reservas.get(reservationId);
        if (reserva == null) {
            logger.warn("Detalle reserva: no existe reservationId={}", reservationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reserva no existe");
        }

        // comprobar que quien solicita existe (Error 401)
        Usuario solicitante = memoria.usuarios.get(userId);
        if (solicitante == null) {
            logger.warn("Detalle reserva: solicitante no existe userId={}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no identificado");
        }

        // comprobar permisos (Error 403)
        // solo podrá si es dueño o admin
        boolean esDueno = reserva.idUsuario().equals(userId);
        boolean esAdmin = solicitante.rol().nombreRol() == NombreRol.ADMIN;

        if (!esDueno && !esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para ver esta reserva.");
        }

        // devolver reserva
        return ResponseEntity.ok(reserva);
    }

    // funcion para comprobar disponibilidad
    private Disponibilidad calcularDisponibilidadPista(Pista pista, LocalDate fecha) {
        // Tomamos que abrimos a las 9 y cerramos a las 22
        LocalTime horaApertura = LocalTime.of(9, 0);
        LocalTime horaCierre = LocalTime.of(22, 0);

        List<FranjaHoraria> franjasLibres = new ArrayList<>();

        // Filtramos reservas de la pista y la fecha, quitando las canceladas
        // las ordenamos por orden de hora de inicio
        List<Reserva> reservasOrdenadas = memoria.reservas.values().stream()
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