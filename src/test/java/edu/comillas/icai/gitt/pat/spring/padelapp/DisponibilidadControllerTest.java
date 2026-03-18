package edu.comillas.icai.gitt.pat.spring.padelapp;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.Estado;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DisponibilidadControllerTest {

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
    void consultarDisponibilidadGlobal_devuelve200YLista() throws Exception {
        crearPistaEnBd("Pista 1", true);
        crearPistaEnBd("Pista 2", true);

        mockMvc.perform(get("/pistaPadel/availability")
                        .param("date", LocalDate.now().plusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void consultarDisponibilidadGlobal_conCourtId_devuelve200YUnaPista() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Central", true);

        mockMvc.perform(get("/pistaPadel/availability")
                        .param("date", LocalDate.now().plusDays(2).toString())
                        .param("courtId", pistaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idPista").value(pistaId));
    }

    @Test
    void consultarDisponibilidadGlobal_fechaPasada_devuelve400() throws Exception {
        crearPistaEnBd("Pista 1", true);

        mockMvc.perform(get("/pistaPadel/availability")
                        .param("date", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarDisponibilidadGlobal_sinFecha_devuelve400() throws Exception {
        crearPistaEnBd("Pista 1", true);

        mockMvc.perform(get("/pistaPadel/availability"))
                .andExpect(status().isInternalServerError());    }

    @Test
    void consultarDisponibilidadPorId_devuelve200() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Buscar", true);

        mockMvc.perform(get("/pistaPadel/courts/{courtId}/availability", pistaId)
                        .param("date", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPista").value(pistaId));
    }

    @Test
    void consultarDisponibilidadPorId_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/pistaPadel/courts/{courtId}/availability", 9999)
                        .param("date", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void consultarDisponibilidadPorId_fechaPasada_devuelve400() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Fecha Pasada", true);

        mockMvc.perform(get("/pistaPadel/courts/{courtId}/availability", pistaId)
                        .param("date", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarDisponibilidadPorId_sinFecha_devuelve400() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Sin Fecha", true);

        mockMvc.perform(get("/pistaPadel/courts/{courtId}/availability", pistaId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void consultarDisponibilidad_reflejaReservaExistente() throws Exception {
        Integer pistaId = crearPistaEnBd("Pista Reservada", true);
        Integer userId = registrarUsuarioEnBdReal("disp@test.com");

        crearReservaEnBd(userId, pistaId, LocalDate.now().plusDays(4), LocalTime.of(10, 0), 60);

        mockMvc.perform(get("/pistaPadel/courts/{courtId}/availability", pistaId)
                        .param("date", LocalDate.now().plusDays(4).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPista").value(pistaId))
                .andExpect(jsonPath("$.franjasDisponibles", not(empty())));
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



    private Integer registrarUsuarioEnBdReal(String email) throws Exception {
        String body = """
                {
                  "nombre": "Ignacio",
                  "apellidos": "Garcia",
                  "email": "%s",
                  "password": "1234",
                  "telefono": "666666666"
                }
                """.formatted(email);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/pistaPadel/auth/register")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        Usuario usuario = repoUsuario.findByEmail(email).orElseThrow();
        return usuario.getIdUsuario();
    }

    private void crearReservaEnBd(Integer userId, Integer pistaId, LocalDate fecha, LocalTime horaInicio, int duracion) {
        Usuario usuario = repoUsuario.findById(userId).orElseThrow();
        Pista pista = repoPista.findById(pistaId).orElseThrow();

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(fecha);
        reserva.setHoraInicio(horaInicio);
        reserva.setDuracionMinutos(duracion);
        reserva.setFechaCreacion(LocalDate.now());
        reserva.setEstado(Estado.ACTIVA);

        repoReserva.save(reserva);
    }
}