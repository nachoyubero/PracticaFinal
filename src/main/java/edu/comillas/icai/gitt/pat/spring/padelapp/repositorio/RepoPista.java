package edu.comillas.icai.gitt.pat.spring.padelapp.repositorio;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface RepoPista extends CrudRepository<Pista, Integer> {

    List<Pista> findByActivaTrue();

}