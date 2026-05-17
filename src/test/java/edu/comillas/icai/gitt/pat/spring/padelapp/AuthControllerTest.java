package edu.comillas.icai.gitt.pat.spring.padelapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void register_ok_devuelve201() throws Exception {
        String body = """
                {
                  "nombre": "Ignacio",
                  "apellidos": "Garcia",
                  "email": "ignacio@test.com",
                  "password": "1234",
                  "telefono": "666666666"
                }
                """;

        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void register_emailDuplicado_devuelve409() throws Exception {
        String body = """
                {
                  "nombre": "Ignacio",
                  "apellidos": "Garcia",
                  "email": "duplicado@test.com",
                  "password": "1234",
                  "telefono": "666666666"
                }
                """;

        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_ok_devuelve200YToken() throws Exception {
        registrarUsuario("login@test.com");

        String body = """
                {
                  "email": "login@test.com",
                  "password": "1234"
                }
                """;

        mockMvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())));
    }

    @Test
    void login_credencialesIncorrectas_devuelve401() throws Exception {
        registrarUsuario("wrong@test.com");

        String body = """
                {
                  "email": "wrong@test.com",
                  "password": "mal"
                }
                """;

        mockMvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_tokenValido_devuelve204() throws Exception {
        String token = registrarYLoguear("logout@test.com");

        mockMvc.perform(post("/pistaPadel/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/pistaPadel/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_tokenValido_devuelve200() throws Exception {
        String token = registrarYLoguear("me@test.com");

        mockMvc.perform(get("/pistaPadel/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@test.com"))
                .andExpect(jsonPath("$.nombre").value("Ignacio"));
    }

    @Test
    void me_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/pistaPadel/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    private void registrarUsuario(String email) throws Exception {
        String body = """
                {
                  "nombre": "Ignacio",
                  "apellidos": "Garcia",
                  "email": "%s",
                  "password": "1234",
                  "telefono": "666666666"
                }
                """.formatted(email);

        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private String registrarYLoguear(String email) throws Exception {
        registrarUsuario(email);

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
}