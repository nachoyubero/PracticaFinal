package edu.comillas.icai.gitt.pat.spring.padelapp.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class TareasProgramadas {

    private static final Logger logger = LoggerFactory.getLogger(TareasProgramadas.class);

    @Autowired
    private JavaMailSender mailSender;

    // Todas las noches a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void enviarRecordatoriosDiarios() {
        logger.info("⏰ [TAREA AUTOMÁTICA 02:00 AM] Iniciando proceso de recordatorios...");

        LocalDate hoy = LocalDate.now();
        logger.debug("Buscando usuarios con reserva para el día: {}", hoy);

        // Creamos el objeto del mensaje
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo("nayubero@gmail.com"); // Prueba con el tuyo para ver que llega
        email.setSubject("Recordatorio de Pista - PadelApp");
        email.setText("¡Hola! Te recordamos que tienes una reserva de pista para hoy: " + hoy);

        // Enviamos el correo de verdad
        try {
            mailSender.send(email);
            logger.info("✅ Correo de recordatorio enviado con éxito.");
        } catch (Exception e) {
            logger.error("❌ Error al enviar el correo: {}", e.getMessage());
        }
    }

    // El primer día del mes a las 10:00 AM
    @Scheduled(cron = "0 0 10 1 * *")
    public void enviarBoletinMensual() {
        logger.info("📅 [TAREA MENSUAL - DÍA 1] Generando boletín de pistas y horarios...");

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo("un_correo_de_prueba@gmail.com");
        email.setSubject("Nuevas Pistas Disponibles - PadelApp");
        email.setText("¡Ya puedes reservar tus pistas para este nuevo mes! Entra en la app para ver los horarios.");

        try {
            mailSender.send(email);
            logger.info("✅ Boletín mensual de pistas enviado.");
        } catch (Exception e) {
            logger.error("❌ Error al enviar el boletín: {}", e.getMessage());
        }
    }
}