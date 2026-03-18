package edu.comillas.icai.gitt.pat.spring.padelapp;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.padelapp.servicios.ServicioPistas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioPistasTest {

    @Mock
    private RepoPista repoPista;

    @InjectMocks
    private ServicioPistas servicioPistas;

    @Test
    void testCrearPistaValidacion() {
        Pista nuevaPista = new Pista();
        nuevaPista.setNombre("Pista Central");

        // Simulamos que al guardar, el repo devuelve la pista con ID
        when(repoPista.save(any(Pista.class))).thenReturn(nuevaPista);

        Pista resultado = servicioPistas.crearPista(nuevaPista);

        assertNotNull(resultado);
        assertEquals("Pista Central", resultado.getNombre());
        verify(repoPista, times(1)).save(nuevaPista);
    }
}
