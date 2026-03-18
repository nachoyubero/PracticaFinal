package edu.comillas.icai.gitt.pat.spring.padelapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Importante

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ServicioAuthTest {

    @Autowired
    private BCryptPasswordEncoder codificador; // Pide el Bean, no la clase de configuración

    @Test
    void testPasswordEncoding() {
        String pass = "1234";
        String encoded = codificador.encode(pass); // Ahora sí funcionará

        assertNotEquals(pass, encoded);
        assertTrue(codificador.matches(pass, encoded));
    }
}
