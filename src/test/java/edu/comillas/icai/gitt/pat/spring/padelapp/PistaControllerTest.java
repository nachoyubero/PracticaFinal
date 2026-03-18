package edu.comillas.icai.gitt.pat.spring.padelapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeRetornarOkAlListarPistas() throws Exception {
        mockMvc.perform(get("/pistas"))
                .andExpect(status().isOk());
    }

    @Test
    void debeDarErrorSiRutaNoExiste() throws Exception {
        mockMvc.perform(get("/api/ruta-falsa"))
                .andExpect(status().isNotFound());
    }
}
