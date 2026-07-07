package it.unina.prenotazioni.entity;

import it.unina.prenotazioni.database.GestorePersistenza;

import java.util.Map;

/**
 * Facade di layer (entity) per l'accesso agli Utenti (Studenti e Bibliotecari).
 */
public class RegistroUtenti {

    private static RegistroUtenti istance;
    private final GestorePersistenza gestorePersistenza;

    private RegistroUtenti() {
        this.gestorePersistenza = new GestorePersistenza();
    }

    public static RegistroUtenti getInstance() {
        if (istance == null) {
            istance = new RegistroUtenti();
        }
        return istance;
    }

    public boolean registraUtente(Utente nuovoUtente) {
        return gestorePersistenza.salva(nuovoUtente);
    }

    public Utente aggiorna(Utente utente) {
        return gestorePersistenza.aggiorna(utente);
    }

    /** Cerca un utente (polimorfo) per e-mail istituzionale; null se non esiste. */
    public Utente cercaUtentePerEmail(String email) {
        return gestorePersistenza.cercaPrimoPerCampi(Utente.class, Map.of("emailIstituzionale", email));
    }

    /** Cerca uno studente per matricola; null se non esiste. */
    public Studente cercaStudentePerMatricola(String matricola) {
        return gestorePersistenza.cercaPrimoPerCampi(Studente.class, Map.of("matricola", matricola));
    }

    /** Carica uno studente per id surrogato; null se non esiste. */
    public Studente trovaStudentePerId(Long id) {
        return gestorePersistenza.trovaPerId(Studente.class, id);
    }

    /** Cerca un bibliotecario per codice identificativo interno; null se non esiste. */
    public Bibliotecario cercaBibliotecarioPerCodice(String codice) {
        return gestorePersistenza.cercaPrimoPerCampi(Bibliotecario.class, Map.of("codiceIdentificativoInterno", codice));
    }

    /** True se esiste già un utente con quella e-mail istituzionale (unicità, RD01/V14). */
    public boolean esisteEmailIstituzionale(String email) {
        return cercaUtentePerEmail(email) != null;
    }
}
