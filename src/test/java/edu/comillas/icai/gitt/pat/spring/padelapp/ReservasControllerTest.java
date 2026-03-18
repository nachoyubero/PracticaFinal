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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReservasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepoUsuario repoUsuario;

    @Autowired
    private RepoPista repoPista;

    @Autowired
    private RepoReserva repoReserva;

    @BeforeEach
    void limpiar() {
        repoReserva.deleteAll();
        repoUsuario.deleteAll();
        repoPista.deleteAll();
    }

    @Test
    void crearReserva_devuelve201() throws Exception {
        Integer userId = registrarUsuario("reserva1@test.com", "Ignacio", "Garcia");
        Integer pistaId = crearPista("Pista Central");

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idReserva").exists())
                .andExpect(jsonPath("$.duracionMinutos").value(60));
    }

    @Test
    void crearReserva_slotOcupado_devuelve409() throws Exception {
        Integer userId = registrarUsuario("reserva2@test.com", "Uno", "Garcia");
        Integer pistaId = crearPista("Pista 2");
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void obtenerMisReservas_devuelve200YLista() throws Exception {
        Integer userId = registrarUsuario("reserva3@test.com", "Dos", "Perez");
        Integer pistaId = crearPista("Pista 3");

        crearReserva(userId, pistaId, LocalDate.now().plusDays(5), "11:00:00", 60);

        mockMvc.perform(get("/pistaPadel/reservations")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerReservaPorId_devuelve200() throws Exception {
        Integer userId = registrarUsuario("reserva4@test.com", "Tres", "Lopez");
        Integer pistaId = crearPista("Pista 4");

        crearReserva(userId, pistaId, LocalDate.now().plusDays(6), "12:00:00", 60);
        Integer reservationId = obtenerPrimeraReservaId();

        mockMvc.perform(get("/pistaPadel/reservations/{reservationId}", reservationId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReserva").value(reservationId));
    }

    @Test
    void modificarReserva_devuelve200YActualizaHora() throws Exception {
        Integer userId = registrarUsuario("reserva5@test.com", "Cuatro", "Ruiz");
        Integer pistaId = crearPista("Pista 5");

        crearReserva(userId, pistaId, LocalDate.now().plusDays(7), "13:00:00", 60);
        Integer reservationId = obtenerPrimeraReservaId();

        String body = """
                {
                  "horaInicio": "15:00:00",
                  "duracionMinutos": 90
                }
                """;

        mockMvc.perform(patch("/pistaPadel/reservations/{reservationId}", reservationId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReserva").value(reservationId))
                .andExpect(jsonPath("$.horaInicio").value("15:00:00"))
                .andExpect(jsonPath("$.duracionMinutos").value(90));
    }

    @Test
    void cancelarReserva_devuelve204() throws Exception {
        Integer userId = registrarUsuario("reserva6@test.com", "Cinco", "Diaz");
        Integer pistaId = crearPista("Pista 6");

        crearReserva(userId, pistaId, LocalDate.now().plusDays(8), "16:00:00", 60);
        Integer reservationId = obtenerPrimeraReservaId();

        mockMvc.perform(delete("/pistaPadel/reservations/{reservationId}", reservationId)
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());
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

    private Integer crearPista(String nombre) {
        Pista pista = new Pista();
        pista.setNombre(nombre);
        pista.setUbicacion("Club Central");
        pista.setPrecioHora(20.0);
        pista.setActiva(true);
        pista.setFechaAlta(LocalDate.now());

        return repoPista.save(pista).getIdPista();
    }

    private void crearReserva(Integer userId, Integer pistaId, LocalDate fecha, String horaInicio, int duracion) throws Exception {
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private Integer obtenerPrimeraReservaId() {
        List<Reserva> reservas = (List<Reserva>) repoReserva.findAll();
        return reservas.get(0).getIdReserva();
    }
}