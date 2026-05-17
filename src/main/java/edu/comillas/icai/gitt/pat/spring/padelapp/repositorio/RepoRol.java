package edu.comillas.icai.gitt.pat.spring.padelapp.repositorio;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RepoRol extends CrudRepository<Rol, Integer> {
    Optional<Rol> findByNombreRol(NombreRol nombreRol);
}