package edu.comillas.icai.gitt.pat.spring.padelapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ServicioAuthTest {

    @Autowired
    private BCryptPasswordEncoder codificador;

    @Test
    void testPasswordEncoding() {
        String pass = "1234";
        String encoded = codificador.encode(pass);

        assertNotEquals(pass, encoded);
        assertTrue(codificador.matches(pass, encoded));
    }
}
