package it.unina.prenotazioni.boundary;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.UtenteDTO;
import org.springframework.web.bind.annotation.*;

/**
 * <<endpoint>> Registrazione (UC1) e Autenticazione (UC2). Nessuna logica applicativa:
 * raccoglie l'input e lo inoltra alla BibliotecaFacade.
 */

@RestController
@RequestMapping("/api/auth")
public class BoundaryAutenticazione {

    /** UC1: registrazione di uno Studente o Bibliotecario (parametri form url-encoded). */
    @PostMapping("/registrazione")
    public UtenteDTO registrazione(@RequestParam("ruolo") String ruolo,
                                   @RequestParam("nome") String nome,
                                   @RequestParam("cognome") String cognome,
                                   @RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("identificativo") String identificativo) {
        return BibliotecaFacade.getInstance()
                .registrazione(ruolo, nome, cognome, email, password, identificativo);
    }

    /** UC2: login con email istituzionale e password. */
    @PostMapping("/login")
    public UtenteDTO autenticazione(@RequestParam("email") String email,
                                    @RequestParam("password") String password) {
        return BibliotecaFacade.getInstance().autenticazione(email, password);
    }
}
