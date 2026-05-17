package edu.comillas.icai.gitt.pat.spring.padelapp;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoUsuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seed(RepoPista repoPista,
                                  RepoRol repoRol,
                                  RepoUsuario repoUsuario,
                                  BCryptPasswordEncoder encoder) {
        return args -> {

            // 1. Crear rol ADMIN si no existe
            Rol rolAdmin = repoRol.findByNombreRol(NombreRol.ADMIN)
                    .orElseGet(() -> {
                        Rol admin = new Rol();
                        admin.setNombreRol(NombreRol.ADMIN);
                        admin.setDescripcion("Administrador");
                        return repoRol.save(admin);
                    });

            // 2. Crear rol USER si no existe
            Rol rolUser = repoRol.findByNombreRol(NombreRol.USER)
                    .orElseGet(() -> {
                        Rol user = new Rol();
                        user.setNombreRol(NombreRol.USER);
                        user.setDescripcion("Usuario");
                        return repoRol.save(user);
                    });

            // 3. Crear usuario administrador si no existe
            if (repoUsuario.findByEmail("admin@padelapp.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Admin");
                admin.setApellidos("PadelApp");
                admin.setEmail("admin@padelapp.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setTelefono("000000000");
                admin.setActivo(true);
                admin.setFechaAlta(LocalDateTime.now());
                admin.setRol(rolAdmin);

                repoUsuario.save(admin);
            }

            // 4. Crear pistas iniciales solo si no hay ninguna
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