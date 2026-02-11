package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Usuario(
        int idUsuario,
        String nombre,
        String apellidos,
        boolean activo,
        LocalDateTime fechaAlta, //Utilizamos localdatetime para saber dia-hora-mins-seg
        String telefono,
        Rol rol,
        String email
) {
}
