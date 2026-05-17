package edu.comillas.icai.gitt.pat.spring.padelapp.servicios;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.Estado;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.padelapp.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.padelapp.repositorio.RepoReserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TareasProgramadas {

    private static final Logger logger = LoggerFactory.getLogger(TareasProgramadas.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RepoReserva repoReserva;

    @Autowired
    private RepoPista repoPista;

    // Todas las noches a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void enviarRecordatoriosDiarios() {
        logger.info("[TAREA 02:00 AM] Iniciando proceso de recordatorios...");
        LocalDate hoy = LocalDate.now();
        logger.debug("Buscando reservas activas para el día: {}", hoy);

        List<Reserva> reservasHoy = ((List<Reserva>) repoReserva.findAll()).stream()
                .filter(r -> r.getFechaReserva().equals(hoy))
                .filter(r -> r.getEstado() != Estado.CANCELADA)
                .toList();

        logger.info("Se encontraron {} reservas para hoy", reservasHoy.size());

        for (Reserva r : reservasHoy) {
            try {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(r.getUsuario().getEmail());
                email.setSubject("Recordatorio de Pista - PadelApp");
                email.setText(
                        "Hola " + r.getUsuario().getNombre() + "!\n\n" +
                                "Te recordamos que tienes reservada la pista " + r.getPista().getNombre() +
                                " a las " + r.getHoraInicio() + " hoy " + hoy + ".\n\n" +
                                "¡Que disfrutes del partido!"
                );
                mailSender.send(email);
                logger.info("Correo de recordatorio enviado a {}", r.getUsuario().getEmail());
            } catch (Exception e) {
                logger.error("Error al enviar correo a {}: {}", r.getUsuario().getEmail(), e.getMessage());
            }
        }

        logger.info("[TAREA 02:00 AM] Proceso de recordatorios finalizado");
    }

    // El primer día del mes a las 10:00 AM
    @Scheduled(cron = "0 0 10 1 * *")
    public void enviarBoletinMensual() {
        logger.info("[TAREA MENSUAL - DÍA 1] Generando boletín de pistas y horarios...");

        List<Pista> pistasActivas = repoPista.findByActivaTrue();
        logger.debug("Pistas activas disponibles: {}", pistasActivas.size());

        String listaPistas = pistasActivas.stream()
                .map(p -> "- " + p.getNombre() + " | " + p.getUbicacion() +
                        " | " + p.getPrecioHora() + "€/hora")
                .collect(Collectors.joining("\n"));

        // Obtenemos todos los emails únicos de usuarios con reservas activas
        List<String> emails = ((List<Reserva>) repoReserva.findAll()).stream()
                .filter(r -> r.getEstado() != Estado.CANCELADA)
                .map(r -> r.getUsuario().getEmail())
                .distinct()
                .toList();

        logger.info("Enviando boletín mensual a {} usuarios", emails.size());

        for (String emailDestino : emails) {
            try {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(emailDestino);
                email.setSubject("Pistas disponibles este mes - PadelApp");
                email.setText(
                        "¡Hola!\n\n" +
                                "Ya puedes reservar tus pistas para este nuevo mes.\n\n" +
                                "Pistas disponibles:\n" + listaPistas + "\n\n" +
                                "Horario del club: 09:00 - 22:00\n\n" +
                                "Entra en la app para hacer tu reserva."
                );
                mailSender.send(email);
                logger.info("Boletín mensual enviado a {}", emailDestino);
            } catch (Exception e) {
                logger.error("Error al enviar boletín a {}: {}", emailDestino, e.getMessage());
            }
        }

        logger.info("[TAREA MENSUAL] Boletín finalizado");
    }
}