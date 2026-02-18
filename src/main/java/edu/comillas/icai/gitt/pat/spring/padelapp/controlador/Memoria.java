package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;

import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Memoria {
    public final Map<Integer, Usuario> usuarios = new HashMap<>();
    public final Map<String, Integer> sesiones = new HashMap<>();
    public final Map<Integer, Pista> pistas = new HashMap<>();
    public final Map<Integer, Reserva> reservas = new HashMap<>();
}
