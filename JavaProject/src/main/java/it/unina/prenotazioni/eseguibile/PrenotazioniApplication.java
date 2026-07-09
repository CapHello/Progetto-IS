package it.unina.prenotazioni.eseguibile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Avvio dell'applicazione. Spring Boot è usato SOLO per il livello web (REST controller
 * e file statici) e per lo scheduler; la persistenza è Hibernate standalone (JpaUtil).
 */
@SpringBootApplication
@EnableScheduling
public class PrenotazioniApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrenotazioniApplication.class, args);
        System.out.println("=====================================================");
        System.out.println("Sistema di Prenotazioni Biblioteca avviato con successo!");
        System.out.println("GUI:  http://localhost:8080/index.html");
        System.out.println("REST: http://localhost:8080/api");
        System.out.println("=====================================================");
    }
}
