package it.unina.prenotazioni;

import it.unina.prenotazioni.boundary.Swing.Login;
import org.springframework.boot.SpringApplication;

import javax.swing.SwingUtilities;

public class Launcher {

    public static void main(String[] args) {
        // 1. FONDAMENTALE: Spring Boot di default disattiva i componenti grafici (headless=true).
        // Dobbiamo forzarlo a false a livello di sistema PRIMA che parta qualsiasi cosa.
        System.setProperty("java.awt.headless", "false");

        // 2. Avvia Spring Boot in un Thread separato per non bloccare l'esecuzione
        Thread springThread = new Thread(() -> {
            SpringApplication.run(PrenotazioniApplication.class, args);
        });

        springThread.start();

        // 3. Avvia la GUI Swing nel suo Event Dispatch Thread dedicato
        SwingUtilities.invokeLater(() -> {
            Login loginFrame = new Login();
            loginFrame.apriLogin();
        });
    }
}