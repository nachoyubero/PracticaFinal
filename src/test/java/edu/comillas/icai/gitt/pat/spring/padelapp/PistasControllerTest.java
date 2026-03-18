package edu.comillas.icai.gitt.pat.spring.padelapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoUsuario;
import edu.comillas.icai.gitt.pat.spring.padelapp.servicios.ServicioAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PistasControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RepoPista repoPista;
    @Autowired private RepoReserva repoReserva;
    @Autowired private RepoUsuario repoUsuario;
    @Autowired private RepoRol repoRol;
    @Autowired private ServicioAuth servicioAuth;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String tokenAdmin;

    @BeforeEach
    void limpiar() {
        repoReserva.deleteAll();
        repoUsuario.deleteAll();
        repoPista.deleteAll();
        tokenAdmin = crearAdminYLoguear();
    }

    @Test
    void crearPista_devuelve201() throws Exception {
        String body = """
                {
                  "nombre": "Pista Central",
                  "ubicacion": "Club Norte",
                  "precioHora": 20.0,
                  "activa": true,
                  "fechaAlta": "%s"
                }
                """.formatted(LocalDate.now());

        mockMvc.perform(post("/pistaPadel/courts")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPista").exists())
                .andExpect(jsonPath("$.nombre").value("Pista Central"))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    void listarPistas_devuelve200YLista() throws Exception {
        crearPistaEnBd("Pista 1", true);
        crearPistaEnBd("Pista 2", true);

        mockMvc.perform(get("/pistaPadel/courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listarPistas_filtrandoActivas_devuelveSoloActivas() throws Exception {
        crearPistaEnBd("Pista Activa", true);
        crearPistaEnBd("Pista Inactiva", false);

        mockMvc.perform(get("/pistaPadel/courts")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Pista Activa"));
    }

    @Test
    void obtenerPistaPorId_devuelve200() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Buscar", true);

        mockMvc.perform(get("/pistaPadel/courts/{id}", pistaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPista").value(pistaId))
                .andExpect(jsonPath("$.nombre").value("Pista Buscar"));
    }

    @Test
    void obtenerPistaPorId_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/pistaPadel/courts/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void modificarPista_devuelve200YActualizaCampos() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Vieja", true);

        String body = """
                {
                  "nombre": "Pista Nueva",
                  "precioHora": 25.5,
                  "activa": false
                }
                """;

        mockMvc.perform(patch("/pistaPadel/courts/{id}", pistaId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pista Nueva"))
                .andExpect(jsonPath("$.precioHora").value(25.5))
                .andExpect(jsonPath("$.activa").value(false));
    }

    @Test
    void modificarPista_inexistente_devuelve404() throws Exception {
        String body = """
                {
                  "nombre": "No Existe"
                }
                """;

        mockMvc.perform(patch("/pistaPadel/courts/{id}", 9999)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarPista_devuelve204() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Borrar", true);

        mockMvc.perform(delete("/pistaPadel/courts/{id}", pistaId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarPista_conReservas_devuelve409() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Ocupada", true);
        Integer userId = crearUsuarioNormal("reserva-pista@test.com");
        crearReservaEnBd(userId, pistaId);

        mockMvc.perform(delete("/pistaPadel/courts/{id}", pistaId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarPista_inexistente_devuelve404() throws Exception {
        mockMvc.perform(delete("/pistaPadel/courts/{id}", 9999)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    // --- HELPERS ---

    private String crearAdminYLoguear() {
        // Creamos el admin directamente en base de datos
        Rol rolAdmin = repoRol.findByNombreRol(NombreRol.ADMIN).orElseThrow();
        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellidos("Test");
        admin.setEmail("admin@test.com");
        admin.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123"));
        admin.setTelefono("000000000");
        admin.setActivo(true);
        admin.setFechaAlta(LocalDateTime.now());
        admin.setRol(rolAdmin);
        repoUsuario.save(admin);

        // Lo logueamos para obtener el token
        return servicioAuth.login(
                new edu.comillas.icai.gitt.pat.spring.padelapp.modelo.LoginRequest(
                        "admin@test.com", "admin123"
                )
        ).token();
    }

    private Integer crearUsuarioNormal(String email) throws Exception {
        String body = """
                {
                  "nombre": "Usuario",
                  "apellidos": "Normal",
                  "email": "%s",
                  "password": "1234",
                  "telefono": "666666666"
                }
                """.formatted(email);
        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
        return repoUsuario.findByEmail(email).orElseThrow().getIdUsuario();
    }

    private Integer crearPistaEnBd(String nombre, boolean activa) {
        Pista pista = new Pista();
        pista.setNombre(nombre);
        pista.setUbicacion("Club Central");
        pista.setPrecioHora(20.0);
        pista.setActiva(activa);
        pista.setFechaAlta(LocalDate.now());
        return repoPista.save(pista).getIdPista();
    }

    private void crearReservaEnBd(Integer userId, Integer pistaId) {
        Usuario usuario = repoUsuario.findById(userId).orElseThrow();
        Pista pista = repoPista.findById(pistaId).orElseThrow();
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.now().plusDays(5));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setDuracionMinutos(60);
        reserva.setFechaCreacion(LocalDate.now());
        repoReserva.save(reserva);
    }
}