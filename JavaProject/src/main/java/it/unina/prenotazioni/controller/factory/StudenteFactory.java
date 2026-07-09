package it.unina.prenotazioni.controller.factory;

import it.unina.prenotazioni.entity.Studente;
import it.unina.prenotazioni.entity.Utente;


/** Creator concreto del Factory Method: costruisce e popola uno Studente (identificativo = matricola). */
public class StudenteFactory implements UtenteFactory {
    @Override
    public Utente creaUtente(String nome, String cognome, String email, String password, String identificativo) {
        Studente studente = new Studente();
        studente.setNome(nome);
        studente.setCognome(cognome);
        studente.setEmailIstituzionale(email);
        studente.setPassword(password);
        studente.setMatricola(identificativo);
        return studente;
    }
}
