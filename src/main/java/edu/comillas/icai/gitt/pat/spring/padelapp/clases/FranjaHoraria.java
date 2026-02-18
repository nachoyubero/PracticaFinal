package edu.comillas.icai.gitt.pat.spring.padelapp.clases;

import java.time.LocalDate;
import java.time.LocalTime;

public class FranjaHoraria {
        private LocalTime inicio;
        private LocalTime fin;

        // Constructor necesario para crear objetos desde el controlador
        public FranjaHoraria(LocalTime inicio, LocalTime fin) {
                this.inicio = inicio;
                this.fin = fin;
        }

        // Getters para que Spring pueda convertir el objeto a JSON
        public LocalTime getInicio() {
                return inicio;
        }

        public LocalTime getFin() {
                return fin;
        }
}
}
