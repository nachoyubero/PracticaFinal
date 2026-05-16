package edu.comillas.icai.gitt.pat.spring.padelapp;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoRol;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seed(RepoPista repoPista, RepoRol repoRol) {
        return args -> {
            // Roles
            if (repoRol.findByNombreRol(NombreRol.ADMIN).isEmpty()) {
                Rol admin = new Rol();
                admin.setNombreRol(NombreRol.ADMIN);
                admin.setDescripcion("Administrador");
                repoRol.save(admin);
            }
            if (repoRol.findByNombreRol(NombreRol.USER).isEmpty()) {
                Rol user = new Rol();
                user.setNombreRol(NombreRol.USER);
                user.setDescripcion("Usuario");
                repoRol.save(user);
            }

            // Pistas (sólo si no hay ninguna)
            if (!repoPista.findAll().iterator().hasNext()) {
                crearPista(repoPista, "Central",  "Calle Principal, 123",    20.0);
                crearPista(repoPista, "Lateral",  "Avenida Secundaria, 456", 15.0);
                crearPista(repoPista, "Exterior", "Parque Central, 789",     10.0);
                crearPista(repoPista, "Cesped",   "Jardín Botánico, 321",    25.0);
            }
        };
    }

    private void crearPista(RepoPista repo, String nombre, String ubic, double precio) {
        Pista p = new Pista();
        p.setNombre(nombre);
        p.setUbicacion(ubic);
        p.setPrecioHora(precio);
        p.setActiva(true);
        p.setFechaAlta(LocalDate.now());
        repo.save(p);
    }
}