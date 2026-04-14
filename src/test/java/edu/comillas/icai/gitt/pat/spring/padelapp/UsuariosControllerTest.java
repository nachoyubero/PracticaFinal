package edu.comillas.icai.gitt.pat.spring.padelapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.LoginRequest;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UsuariosControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RepoUsuario repoUsuario;
    @Autowired private RepoRol repoRol;
    @Autowired private ServicioAuth servicioAuth;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String tokenAdmin;

    @BeforeEach
    void limpiarUsuarios() {
        repoUsuario.deleteAll();
        tokenAdmin = crearAdminYLoguear();
    }

    @Test
    void getUsers_devuelve200YListaUsuarios() throws Exception {
        registrarUsuario("uno@test.com", "Uno", "Garcia");
        registrarUsuario("dos@test.com", "Dos", "Perez");

        // Son 3 en total: admin + los dos registrados
        mockMvc.perform(get("/pistaPadel/users")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getUserById_devuelve200YUsuario() throws Exception {
        String token = registrarYLoguear("buscar@test.com", "Ignacio", "Garcia");
        Integer userId = obtenerIdUsuarioPorEmail("buscar@test.com");

        mockMvc.perform(get("/pistaPadel/users/{id}", userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(userId))
                .andExpect(jsonPath("$.nombre").value("Ignacio"))
                .andExpect(jsonPath("$.apellidos").value("Garcia"))
                .andExpect(jsonPath("$.email").value("buscar@test.com"));
    }

    @Test
    void getUserById_inexistente_devuelve404() throws Exception {
        // Necesita ADMIN para acceder a un id que no es el suyo
        mockMvc.perform(get("/pistaPadel/users/{id}", 9999)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchUser_devuelve200YActualizaNombreYApellidos() throws Exception {
        String token = registrarYLoguear("modificar@test.com", "NombreViejo", "ApellidoViejo");
        Integer userId = obtenerIdUsuarioPorEmail("modificar@test.com");

        String body = """
                {
                  "nombre": "NombreNuevo",
                  "apellidos": "ApellidoNuevo"
                }
                """;

        mockMvc.perform(patch("/pistaPadel/users/{id}", userId)
                        .header("Authorization", "Bearer " + token)
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
        String token = registrarYLoguear("segundo@test.com", "Segundo", "Dos");
        Integer segundoId = obtenerIdUsuarioPorEmail("segundo@test.com");

        String body = """
                {
                  "email": "primero@test.com"
                }
                """;

        mockMvc.perform(patch("/pistaPadel/users/{id}", segundoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void patchUser_inexistente_devuelve404() throws Exception {
        // Necesita ADMIN para modificar un id que no es el suyo
        String body = """
                {
                  "nombre": "NoExiste"
                }
                """;

        mockMvc.perform(patch("/pistaPadel/users/{id}", 9999)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // --- HELPERS ---

    private String crearAdminYLoguear() {
        Rol rolAdmin = repoRol.findByNombreRol(NombreRol.ADMIN).orElseThrow();
        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellidos("Test");
        admin.setEmail("admin@test.com");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
        admin.setTelefono("000000000");
        admin.setActivo(true);
        admin.setFechaAlta(LocalDateTime.now());
        admin.setRol(rolAdmin);
        repoUsuario.save(admin);

        return servicioAuth.login(
                new LoginRequest("admin@test.com", "admin123")
        ).token();
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

    private String registrarYLoguear(String email, String nombre, String apellidos) throws Exception {
        registrarUsuario(email, nombre, apellidos);
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

    private Integer obtenerIdUsuarioPorEmail(String email) {
        return repoUsuario.findByEmail(email).orElseThrow().getIdUsuario();
    }
}