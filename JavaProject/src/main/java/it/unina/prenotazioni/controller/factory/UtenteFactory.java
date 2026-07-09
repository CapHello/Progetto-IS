package it.unina.prenotazioni.controller.factory;

import it.unina.prenotazioni.entity.Utente;

/**
 * Factory Method (UC1): incapsula la creazione della sottoclasse di Utente
 * corretta in base al ruolo scelto in fase di registrazione.
 */
public interface UtenteFactory {
    Utente creaUtente(String nome, String cognome, String email, String password, String identificativo);
}
