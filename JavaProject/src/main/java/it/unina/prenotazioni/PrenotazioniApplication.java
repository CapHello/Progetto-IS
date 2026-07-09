package it.unina.prenotazioni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Avvio dell'applicazione. Spring Boot è usato SOLO per il livello web (endpoint REST
 * e pagine statiche in resources/static) e per lo scheduler (@EnableScheduling);
 * la persistenza è Hibernate standalone (JpaUtil), indipendente da Spring.
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
