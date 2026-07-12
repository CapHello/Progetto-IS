package it.unina.prenotazioni.boundary.SpringBoot;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.UtenteDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registrazione (UC1) e Autenticazione (UC2). Nessuna logica applicativa:
 * raccoglie l'input e lo inoltra alla BibliotecaFacade.
 */

@RestController
@RequestMapping("/api/auth")
public class BoundaryAutenticazione {

    /**
     * UC1: registrazione di uno Studente o Bibliotecario (parametri form url-encoded).
     * @param ruolo ruolo
     * @param nome nome
     * @param cognome cognome
     * @param email email
     * @param password password
     * @param identificativo identificativo
     */
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

    /**
     * UC2: login con email istituzionale e password.
     * @param email email
     * @param password password
     */
    @PostMapping("/login")
    public UtenteDTO autenticazione(@RequestParam("email") String email,
                                    @RequestParam("password") String password) {
        return BibliotecaFacade.getInstance().autenticazione(email, password);
    }
}
