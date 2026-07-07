package it.unina.prenotazioni.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce le eccezioni di dominio in risposte HTTP con corpo JSON {"errore": "..."},
 * così che la GUI possa mostrare messaggi chiari. Analogo agli exception handler di Flask.
 */
@RestControllerAdvice
public class GestoreEccezioni {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> gestisciRichiestaNonValida(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> gestisciNonAutorizzato(SecurityException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errore", e.getMessage()));
    }
}
