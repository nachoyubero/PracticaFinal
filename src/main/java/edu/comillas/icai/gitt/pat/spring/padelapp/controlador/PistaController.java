package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Aqui se van a realizar los endpoints
@RestController
@RequestMapping("/pistaPadel") //Comenzamos a definir nuestra base de URL
public class PistaController {
    HashMap<Integer, Usuario> usuarios = new HashMap<>(); //Simulamos una base de datos de usuarios
    HashMap<Integer, Pista> pistas = new HashMap<>();
    // Como no tenemos base de datos, vamos a simular una lista de usuarios con un HashMap.
    public PistaController() {
        Rol rolDeAdmin = new Rol(1, NombreRol.ADMIN, "Administrador del sistema");
        Rol rolDeUsuario = new Rol(2, NombreRol.USER, "Jugador normal");

        usuarios.put(1, new Usuario(1, "Pepe", "García", true, LocalDateTime.now(), "600111222", rolDeAdmin, "admin@test.com"));
        usuarios.put(2, new Usuario(2, "Laura", "López", true, LocalDateTime.now(), "600333444", rolDeUsuario, "laura@test.com"));
    }

    // Creación del endpoint que crea una nueva pista de padel
    @PostMapping("/courts")
    public ResponseEntity<Pista> crearPista(@RequestBody Pista pista){
        //Aqui se va a crear la pista, pero como no tenemos la capa de servicio ni la de repositorio, vamos a simularlo
        Pista nuevaPista = new Pista(1, pista.nombre(), pista.ubicacion(), pista.precioHora(), true, pista.fechaAlta());
        pistas.put(1, nuevaPista); //Guardamos la pista en el HashMap de pistas porque todavía no tenemos persistencia
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPista);
    }

    //Endpoint que devuelve los usuarios si la peticción es de un admin
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> obtenerUsuarios(@RequestBody Usuario usuarioSolicitante) {
        if (usuarioSolicitante.rol().nombreRol() == NombreRol.ADMIN) {
            List<Usuario> listaUsuarios = new ArrayList<>(usuarios.values());
            return ResponseEntity.ok(listaUsuarios);
        } else { return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Aquí se realiza el endpoint GET para obtener usuario por idUsuario
    @GetMapping("/users/{idUsuario}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable int idUsuario, @RequestBody(required = false) Usuario usuarioSolicitante) {
            if (usuarioSolicitante.rol().nombreRol() == NombreRol.ADMIN) {
                if (usuarios.containsKey(idUsuario)) {
                    Usuario usuarioBuscado = usuarios.get(idUsuario);
                    return ResponseEntity.status(HttpStatus.OK).body(usuarioBuscado);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            } else return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }





}
