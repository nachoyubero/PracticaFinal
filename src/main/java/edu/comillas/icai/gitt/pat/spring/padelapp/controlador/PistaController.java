package edu.comillas.icai.gitt.pat.spring.padelapp.controlador;


import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Aqui se van a realizar los siguientes endpoints:
//
@RestController
@RequestMapping("/pistaPadel") //Comenzamos a definir nuestra base de URL
public class PistaController {

    @PostMapping("/courts")
    public ResponseEntity<Pista> crearPista(@RequestBody Pista pista) {}

}
