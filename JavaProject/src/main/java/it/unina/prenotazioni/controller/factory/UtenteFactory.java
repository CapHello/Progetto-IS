package it.unina.prenotazioni.controller.factory;

import it.unina.prenotazioni.entity.Utente;

public interface UtenteFactory {
    Utente creaUtente(String nome, String cognome, String email, String password, String identificativo);
}
