package edu.comillas.icai.gitt.pat.spring.padelapp;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UsuariosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepoUsuario repoUsuario;

    @BeforeEach
    void limpiarUsuarios() {
        repoUsuario.deleteAll();
    }

    @Test
    void getUsers_devuelve200YListaUsuarios() throws Exception {
        registrarUsuario("uno@test.com", "Uno", "Garcia");
        registrarUsuario("dos@test.com", "Dos", "Perez");

        mockMvc.perform(get("/pistaPadel/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getUserById_devuelve200YUsuario() throws Exception {
        registrarUsuario("buscar@test.com", "Ignacio", "Garcia");
        Integer userId = obtenerIdUsuarioPorEmail("buscar@test.com");

        mockMvc.perform(get("/pistaPadel/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(userId))
                .andExpect(jsonPath("$.nombre").value("Ignacio"))
                .andExpect(jsonPath("$.apellidos").value("Garcia"))
                .andExpect(jsonPath("$.email").value("buscar@test.com"));
    }

    @Test
    void getUserById_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/pistaPadel/users/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchUser_devuelve200YActualizaNombreYApellidos() throws Exception {
        registrarUsuario("modificar@test.com", "NombreViejo", "ApellidoViejo");
        Integer userId = obtenerIdUsuarioPorEmail("modificar@test.com");

        String body = """
                {
                  "nombre": "NombreNuevo",
                  "apellidos": "ApellidoNuevo"
                }
                """;

        mockMvc.perform(patch("/pistaPadel/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(userId))
                .andExpect(jsonPath("$.nombre").value("NombreNuevo"))
                .andExpect(jsonPath("$.apellidos").value("ApellidoNuevo"))
                .andExpect(jsonPath("$.email").value("modificar@test.com"));
    }

    @Test
    void patchUser_emailDuplicado_devuelve409() throws Exception {
        registrarUsuario("primero@test.com", "Primero", "Uno");
        registrarUsuario("segundo@test.com", "Segundo", "Dos");

        Integer segundoId = obtenerIdUsuarioPorEmail("segundo@test.com");

        String body = """
                {
                  "email": "primero@test.com"
                }
                """;

        mockMvc.perform(patch("/pistaPadel/users/{id}", segundoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void patchUser_inexistente_devuelve404() throws Exception {
        String body = """
                {
                  "nombre": "NoExiste"
                }
                """;

        mockMvc.perform(patch("/pistaPadel/users/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    private void registrarUsuario(String email, String nombre, String apellidos) throws Exception {
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
    }

    private Integer obtenerIdUsuarioPorEmail(String email) {
        Usuario usuario = repoUsuario.findByEmail(email).orElseThrow();
        return usuario.getIdUsuario();
    }
}