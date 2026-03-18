package edu.comillas.icai.gitt.pat.spring.padelapp;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PistasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepoPista repoPista;

    @Autowired
    private RepoReserva repoReserva;

    @Autowired
    private RepoUsuario repoUsuario;

    @BeforeEach
    void limpiar() {
        repoReserva.deleteAll();
        repoUsuario.deleteAll();
        repoPista.deleteAll();
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPista").exists())
                .andExpect(jsonPath("$.nombre").value("Pista Central"))
                .andExpect(jsonPath("$.ubicacion").value("Club Norte"))
                .andExpect(jsonPath("$.precioHora").value(20.0))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPista").value(pistaId))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarPista_devuelve204() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Borrar", true);

        mockMvc.perform(delete("/pistaPadel/courts/{id}", pistaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarPista_conReservas_devuelve409() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Ocupada", true);
        Integer userId = registrarUsuario("reserva-pista@test.com", "Ignacio", "Garcia");

        crearReservaEnBd(userId, pistaId);

        mockMvc.perform(delete("/pistaPadel/courts/{id}", pistaId))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarPista_inexistente_devuelve404() throws Exception {
        mockMvc.perform(delete("/pistaPadel/courts/{id}", 9999))
                .andExpect(status().isNotFound());
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

    private Integer registrarUsuario(String email, String nombre, String apellidos) throws Exception {
        String body = """
                {
                  "nombre": "%s",
                  "apellidos": "%s",
                  "email": "%s",
                  "password": "1234",
                  "telefono": "666666666"
                }
                """.formatted(nombre, apellidos, email);

        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        Usuario usuario = repoUsuario.findByEmail(email).orElseThrow();
        return usuario.getIdUsuario();
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