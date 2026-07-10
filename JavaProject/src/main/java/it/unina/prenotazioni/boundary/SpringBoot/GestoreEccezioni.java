package it.unina.prenotazioni.boundary.SpringBoot;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * <<endpoint>> Handler globale delle eccezioni: traduce le eccezioni di dominio in
 * risposte HTTP con corpo JSON {"errore": "..."}, che la GUI mostra all'utente.
 * I gestori comunicano gli esiti negativi tramite eccezioni, mai tramite codici HTTP.
 */
@RestControllerAdvice
public class GestoreEccezioni {

    /** Dati non validi o operazione non consentita nello stato attuale: risposta 400 Bad Request. */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> gestisciRichiestaNonValida(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
    }

    /** Credenziali errate o account bloccato: risposta 401 Unauthorized. */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> gestisciNonAutorizzato(SecurityException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errore", e.getMessage()));
    }
}
