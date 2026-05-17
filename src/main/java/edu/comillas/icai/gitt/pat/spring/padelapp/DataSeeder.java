package edu.comillas.icai.gitt.pat.spring.padelapp;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder implements org.springframework.boot.CommandLineRunner {

    @Autowired private RepoRol repoRol;
    @Autowired private RepoPista repoPista;
    @Autowired private RepoUsuario repoUsuario;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
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

        // Usuario admin
        if (repoUsuario.findByEmail("admin@padelapp.com").isEmpty()) {
            Rol rolAdmin = repoRol.findByNombreRol(NombreRol.ADMIN).get();
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellidos("PadelApp");
            admin.setEmail("admin@padelapp.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setActivo(true);
            admin.setFechaAlta(LocalDateTime.now());
            admin.setRol(rolAdmin);
            repoUsuario.save(admin);
        }

        // Pistas
        if (repoPista.count() == 0) {
            crearPista("Central",  "Calle Principal, 123",   20.0);
            crearPista("Lateral",  "Avenida Secundaria, 456", 15.0);
            crearPista("Exterior", "Parque Central, 789",    10.0);
            crearPista("Cesped",   "Jardín Botánico, 321",   25.0);
        }
    }

    private void crearPista(String nombre, String ubicacion, double precioHora) {
        Pista p = new Pista();
        p.setNombre(nombre);
        p.setUbicacion(ubicacion);
        p.setPrecioHora(precioHora);
        p.setActiva(true);
        p.setFechaAlta(LocalDate.now());
        repoPista.save(p);
    }
}