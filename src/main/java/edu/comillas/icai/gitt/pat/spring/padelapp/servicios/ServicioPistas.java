package edu.comillas.icai.gitt.pat.spring.padelapp.servicios;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.Estado;
import edu.comillas.icai.gitt.pat.spring.padelapp.clases.FranjaHoraria;
import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.*;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ServicioPistas {

    @Autowired private RepoPista repoPista;
    @Autowired private RepoReserva repoReserva;
    @Autowired private RepoUsuario repoUsuario;

    // --- PISTAS ---
    public Pista crearPista(Pista pista) { return repoPista.save(pista); }
    public List<Pista> listarPistas(Boolean active) {
        if (active != null && active) return repoPista.findByActivaTrue();
        return (List<Pista>) repoPista.findAll();
    }
    public Pista obtenerPista(Integer courtId) {
        return repoPista.findById(courtId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));
    }
    public Pista modificarPista(Integer courtId, Pista datos) {
        Pista existente = obtenerPista(courtId);
        if (datos.getNombre() != null) existente.setNombre(datos.getNombre());
        if (datos.getActiva() != null) existente.setActiva(datos.getActiva());
        if (datos.getPrecioHora() != null) existente.setPrecioHora(datos.getPrecioHora());
        return repoPista.save(existente);
    }
    public void eliminarPista(Integer courtId) {
        Pista pista = obtenerPista(courtId);
        boolean tieneReservas = ((List<Reserva>) repoReserva.findAll()).stream()
                .anyMatch(r -> r.getPista().getIdPista().equals(courtId));
        if (tieneReservas) throw new ResponseStatusException(HttpStatus.CONFLICT, "Pista tiene reservas");
        repoPista.delete(pista);
    }

    // --- DISPONIBILIDAD ---
    public List<Disponibilidad> consultarDisponibilidadGlobal(LocalDate date) {
        if (date.isBefore(LocalDate.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha pasada");
        List<Disponibilidad> resultado = new ArrayList<>();
        repoPista.findByActivaTrue().forEach(p -> resultado.add(calcularDisponibilidadPista(p, date)));
        return resultado;
    }

    public Disponibilidad consultarDisponibilidadPistaId(Integer courtId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha pasada");
        Pista pista = obtenerPista(courtId);
        return calcularDisponibilidadPista(pista, date);
    }

    // --- RESERVAS ---
    public Reserva crearReserva(Reserva entrada) {
        Pista pista = obtenerPista(entrada.getPista().getIdPista());
        Usuario usuario = repoUsuario.findById(entrada.getUsuario().getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe"));

        if (LocalDateTime.of(entrada.getFechaReserva(), entrada.getHoraInicio()).isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha futura obligatoria");
        }

        Disponibilidad disp = calcularDisponibilidadPista(pista, entrada.getFechaReserva());
        LocalTime horaFinSolicitada = entrada.getHoraInicio().plusMinutes(entrada.getDuracionMinutos());

        boolean hayHueco = disp.franjasDisponibles().stream().anyMatch(f ->
                !entrada.getHoraInicio().isBefore(f.getInicio()) && !horaFinSolicitada.isAfter(f.getFin())
        );
        if (!hayHueco) throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay disponibilidad");

        entrada.setPista(pista);
        entrada.setUsuario(usuario);
        entrada.setEstado(Estado.ACTIVA);
        entrada.setFechaCreacion(LocalDate.now());
        return repoReserva.save(entrada);
    }

    // NUEVO: Mis Reservas
    public List<Reserva> obtenerMisReservas(Integer userId, LocalDate from, LocalDate to) {
        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no existe"));

        return repoReserva.findByUsuario(usuario).stream()
                .filter(r -> from == null || !r.getFechaReserva().isBefore(from))
                .filter(r -> to == null || !r.getFechaReserva().isAfter(to))
                .sorted(Comparator.comparing(Reserva::getFechaReserva).thenComparing(Reserva::getHoraInicio))
                .toList();
    }

    // NUEVO: Detalle de reserva
    public Reserva obtenerReserva(Integer reservationId, Integer userId) {
        Reserva reserva = repoReserva.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe"));
        Usuario solicitante = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no identificado"));

        boolean esDueno = reserva.getUsuario().getIdUsuario().equals(userId);
        boolean esAdmin = solicitante.getRol().getNombreRol() == NombreRol.ADMIN;
        if (!esDueno && !esAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos");

        return reserva;
    }

    // NUEVO: Modificar/Reprogramar Reserva
    public Reserva modificarReserva(Integer reservationId, Reserva nuevosDatos, Integer userId) {
        Reserva existente = obtenerReserva(reservationId, userId); // Ya comprueba si existe y si tiene permiso

        LocalDate nuevaFecha = nuevosDatos.getFechaReserva() != null ? nuevosDatos.getFechaReserva() : existente.getFechaReserva();
        LocalTime nuevaHora = nuevosDatos.getHoraInicio() != null ? nuevosDatos.getHoraInicio() : existente.getHoraInicio();
        Integer nuevaDuracion = nuevosDatos.getDuracionMinutos() != null ? nuevosDatos.getDuracionMinutos() : existente.getDuracionMinutos();

        // Comprobar conflicto (que no pise otra reserva)
        if (nuevosDatos.getFechaReserva() != null || nuevosDatos.getHoraInicio() != null) {
            LocalTime finNuevo = nuevaHora.plusMinutes(nuevaDuracion);
            boolean pistaOcupada = repoReserva.findByPistaAndFechaReserva(existente.getPista(), nuevaFecha).stream()
                    .filter(r -> !r.getIdReserva().equals(reservationId)) // Excluir la propia reserva
                    .anyMatch(r -> nuevaHora.isBefore(r.getHoraFin()) && finNuevo.isAfter(r.getHoraInicio()));

            if (pistaOcupada) throw new ResponseStatusException(HttpStatus.CONFLICT, "El nuevo horario ya está ocupado");
        }

        existente.setFechaReserva(nuevaFecha);
        existente.setHoraInicio(nuevaHora);
        existente.setDuracionMinutos(nuevaDuracion);
        if (nuevosDatos.getEstado() != null) existente.setEstado(nuevosDatos.getEstado());

        return repoReserva.save(existente);
    }

    public void cancelarReserva(Integer idReserva, Integer userId) {
        Reserva reserva = obtenerReserva(idReserva, userId);
        if (LocalDateTime.now().plusHours(24).isAfter(LocalDateTime.of(reserva.getFechaReserva(), reserva.getHoraInicio()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Menos de 24h");
        }
        repoReserva.delete(reserva);
    }

    // NUEVO: Reservas Admin
    public List<Reserva> obtenerReservasAdmin(Integer adminId, LocalDate date, Integer courtId, Integer userId) {
        Usuario admin = repoUsuario.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (admin.getRol().getNombreRol() != NombreRol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN");
        }

        List<Reserva> todas = (List<Reserva>) repoReserva.findAll();
        return todas.stream()
                .filter(r -> date == null || r.getFechaReserva().equals(date))
                .filter(r -> courtId == null || r.getPista().getIdPista().equals(courtId))
                .filter(r -> userId == null || r.getUsuario().getIdUsuario().equals(userId))
                .toList();
    }

    // Lógica privada
    private Disponibilidad calcularDisponibilidadPista(Pista pista, LocalDate fecha) {
        LocalTime ultimoFin = LocalTime.of(9, 0);
        LocalTime horaCierre = LocalTime.of(22, 0);
        List<FranjaHoraria> franjasLibres = new ArrayList<>();

        List<Reserva> reservasOrdenadas = repoReserva.findByPistaAndFechaReserva(pista, fecha).stream()
                .filter(r -> r.getEstado() != Estado.CANCELADA)
                .sorted(Comparator.comparing(Reserva::getHoraInicio))
                .toList();

        for (Reserva r : reservasOrdenadas) {
            if (ultimoFin.isBefore(r.getHoraInicio())) franjasLibres.add(new FranjaHoraria(ultimoFin, r.getHoraInicio()));
            ultimoFin = r.getHoraFin();
        }
        if (ultimoFin.isBefore(horaCierre)) franjasLibres.add(new FranjaHoraria(ultimoFin, horaCierre));

        return new Disponibilidad(pista.getIdPista(), fecha, franjasLibres);
    }
}