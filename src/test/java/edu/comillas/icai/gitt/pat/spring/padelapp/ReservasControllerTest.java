package edu.comillas.icai.gitt.pat.spring.padelapp;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReservasControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RepoUsuario repoUsuario;
    @Autowired private RepoPista repoPista;
    @Autowired private RepoReserva repoReserva;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void limpiar() {
        repoReserva.deleteAll();
        repoUsuario.deleteAll();
        repoPista.deleteAll();
    }

    @Test
    void crearReserva_devuelve201() throws Exception {
        String token = registrarYLoguear("reserva1@test.com", "Ignacio", "Garcia");
        Integer pistaId = crearPista("Pista Central");

        Integer userId = repoUsuario.findByEmail("reserva1@test.com").orElseThrow().getIdUsuario();

        String body = """
                {
                  "usuario": { "idUsuario": %d },
                  "pista": { "idPista": %d },
                  "fechaReserva": "%s",
                  "horaInicio": "10:00:00",
                  "duracionMinutos": 60
                }
                """.formatted(userId, pistaId, LocalDate.now().plusDays(3));

        mockMvc.perform(post("/pistaPadel/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idReserva").exists())
                .andExpect(jsonPath("$.duracionMinutos").value(60));
    }

    @Test
    void crearReserva_slotOcupado_devuelve409() throws Exception {
        String token = registrarYLoguear("reserva2@test.com", "Uno", "Garcia");
        Integer pistaId = crearPista("Pista 2");
        Integer userId = repoUsuario.findByEmail("reserva2@test.com").orElseThrow().getIdUsuario();
        String fecha = LocalDate.now().plusDays(4).toString();

        String body = """
                {
                  "usuario": { "idUsuario": %d },
                  "pista": { "idPista": %d },
                  "fechaReserva": "%s",
                  "horaInicio": "10:00:00",
                  "duracionMinutos": 60
                }
                """.formatted(userId, pistaId, fecha);

        mockMvc.perform(post("/pistaPadel/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/pistaPadel/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void obtenerMisReservas_devuelve200YLista() throws Exception {
        String token = registrarYLoguear("reserva3@test.com", "Dos", "Perez");
        Integer pistaId = crearPista("Pista 3");
        Integer userId = repoUsuario.findByEmail("reserva3@test.com").orElseThrow().getIdUsuario();
        crearReserva(token, userId, pistaId, LocalDate.now().plusDays(5), "11:00:00", 60);

        mockMvc.perform(get("/pistaPadel/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerReservaPorId_devuelve200() throws Exception {
        String token = registrarYLoguear("reserva4@test.com", "Tres", "Lopez");
        Integer pistaId = crearPista("Pista 4");
        Integer userId = repoUsuario.findByEmail("reserva4@test.com").orElseThrow().getIdUsuario();
        crearReserva(token, userId, pistaId, LocalDate.now().plusDays(6), "12:00:00", 60);

        Integer reservationId = obtenerPrimeraReservaId();

        mockMvc.perform(get("/pistaPadel/reservations/{reservationId}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReserva").value(reservationId));
    }

    @Test
    void modificarReserva_devuelve200YActualizaHora() throws Exception {
        String token = registrarYLoguear("reserva5@test.com", "Cuatro", "Ruiz");
        Integer pistaId = crearPista("Pista 5");
        Integer userId = repoUsuario.findByEmail("reserva5@test.com").orElseThrow().getIdUsuario();
        crearReserva(token, userId, pistaId, LocalDate.now().plusDays(7), "13:00:00", 60);

        Integer reservationId = obtenerPrimeraReservaId();

        String body = """
                {
                  "horaInicio": "15:00:00",
                  "duracionMinutos": 90
                }
                """;

        mockMvc.perform(patch("/pistaPadel/reservations/{reservationId}", reservationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReserva").value(reservationId))
                .andExpect(jsonPath("$.horaInicio").value("15:00:00"))
                .andExpect(jsonPath("$.duracionMinutos").value(90));
    }

    @Test
    void cancelarReserva_devuelve204() throws Exception {
        String token = registrarYLoguear("reserva6@test.com", "Cinco", "Diaz");
        Integer pistaId = crearPista("Pista 6");
        Integer userId = repoUsuario.findByEmail("reserva6@test.com").orElseThrow().getIdUsuario();
        crearReserva(token, userId, pistaId, LocalDate.now().plusDays(8), "16:00:00", 60);

        Integer reservationId = obtenerPrimeraReservaId();

        mockMvc.perform(delete("/pistaPadel/reservations/{reservationId}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // --- HELPERS ---

    private String registrarYLoguear(String email, String nombre, String apellidos) throws Exception {
        String registerBody = """
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
                        .content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {
                  "email": "%s",
                  "password": "1234"
                }
                """.formatted(email);

        String response = mockMvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private Integer crearPista(String nombre) {
        Pista pista = new Pista();
        pista.setNombre(nombre);
        pista.setUbicacion("Club Central");
        pista.setPrecioHora(20.0);
        pista.setActiva(true);
        pista.setFechaAlta(LocalDate.now());
        return repoPista.save(pista).getIdPista();
    }

    private void crearReserva(String token, Integer userId, Integer pistaId,
                              LocalDate fecha, String horaInicio, int duracion) throws Exception {
        String body = """
                {
                  "usuario": { "idUsuario": %d },
                  "pista": { "idPista": %d },
                  "fechaReserva": "%s",
                  "horaInicio": "%s",
                  "duracionMinutos": %d
                }
                """.formatted(userId, pistaId, fecha, horaInicio, duracion);

        mockMvc.perform(post("/pistaPadel/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private Integer obtenerPrimeraReservaId() {
        List<Reserva> reservas = (List<Reserva>) repoReserva.findAll();
        return reservas.get(0).getIdReserva();
    }
}