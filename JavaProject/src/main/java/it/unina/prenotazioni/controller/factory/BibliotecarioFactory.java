package it.unina.prenotazioni.controller.factory;

import it.unina.prenotazioni.entity.Bibliotecario;
import it.unina.prenotazioni.entity.Utente;


/** Creator concreto del Factory Method: costruisce e popola un Bibliotecario (identificativo = codice interno). */
public class BibliotecarioFactory implements UtenteFactory {
    @Override
    public Utente creaUtente(String nome, String cognome, String email, String password, String identificativo) {
        Bibliotecario bibliotecario = new Bibliotecario();
        bibliotecario.setNome(nome);
        bibliotecario.setCognome(cognome);
        bibliotecario.setEmailIstituzionale(email);
        bibliotecario.setPassword(password);
        bibliotecario.setCodiceIdentificativoInterno(identificativo);
        return bibliotecario;
    }
}
