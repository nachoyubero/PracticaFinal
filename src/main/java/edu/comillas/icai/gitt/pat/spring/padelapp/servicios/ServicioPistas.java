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

    public Pista crearPista(Pista pista) {
        return repoPista.save(pista);
    }

    public List<Pista> listarPistas(Boolean active) {
        if (active != null && active) return repoPista.findByActivaTrue();
        return (List<Pista>) repoPista.findAll();
    }

    public Pista obtenerPista(Integer courtId) {
        return repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));
    }

    public Pista modificarPista(Integer courtId, Pista datos) {
        Pista existente = obtenerPista(courtId);
        if (datos.getNombre() != null) existente.setNombre(datos.getNombre());
        if (datos.getActiva() != null) existente.setActiva(datos.getActiva());
        // (Añade el resto de campos si hace falta)
        return repoPista.save(existente);
    }

    public void eliminarPista(Integer courtId) {
        Pista pista = obtenerPista(courtId);
        boolean tieneReservas = ((List<Reserva>) repoReserva.findAll()).stream()
                .anyMatch(r -> r.getPista().getIdPista().equals(courtId));
        if (tieneReservas) throw new ResponseStatusException(HttpStatus.CONFLICT, "Pista tiene reservas");
        repoPista.delete(pista);
    }

    // --- LÓGICA DE RESERVAS ---

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

    public void cancelarReserva(Integer idReserva, Integer userId) {
        Reserva reserva = repoReserva.findById(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario solicitante = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean esDueno = reserva.getUsuario().getIdUsuario().equals(userId);
        boolean esAdmin = solicitante.getRol().getNombreRol() == NombreRol.ADMIN;

        if (!esDueno && !esAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        if (LocalDateTime.now().plusHours(24).isAfter(LocalDateTime.of(reserva.getFechaReserva(), reserva.getHoraInicio()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Menos de 24h");
        }
        repoReserva.delete(reserva);
    }

    public List<Disponibilidad> consultarDisponibilidad(LocalDate date, Integer courtId) {
        if (date.isBefore(LocalDate.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha pasada");
        List<Disponibilidad> resultado = new ArrayList<>();
        if (courtId != null) {
            resultado.add(calcularDisponibilidadPista(obtenerPista(courtId), date));
        } else {
            repoPista.findByActivaTrue().forEach(p -> resultado.add(calcularDisponibilidadPista(p, date)));
        }
        return resultado;
    }

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
