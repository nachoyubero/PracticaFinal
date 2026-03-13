package edu.comillas.icai.gitt.pat.spring.padelapp.repositorio;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface RepoUsuario extends CrudRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

}
