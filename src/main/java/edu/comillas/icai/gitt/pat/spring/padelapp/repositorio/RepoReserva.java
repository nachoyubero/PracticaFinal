package edu.comillas.icai.gitt.pat.spring.padelapp.repositorio;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface RepoReserva extends CrudRepository<Reserva, Integer> {

    List<Reserva> findByUsuario(Usuario usuario);

    List<Reserva> findByPistaAndFechaReserva(Pista pista, LocalDate fechaReserva);

}