package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * <<Facade>> <<Singleton>>: unico punto d'accesso al controller per le boundary.
 * Espone un'operazione per caso d'uso e delega ai quattro gestori; le firme usano
 * solo tipi primitivi e DTO (nessun tipo entity attraversa il confine del layer).
 */
public class BibliotecaFacade {

    private static BibliotecaFacade instance;

    private BibliotecaFacade() {}

    public static BibliotecaFacade getInstance() {
        if (instance == null) {
            instance = new BibliotecaFacade();
        }
        return instance;
    }

    // --- GestoreUtenti (UC1, UC2, UC8) ---

    /** UC1: registra un nuovo Studente o Bibliotecario. */
    public UtenteDTO registrazione(String ruolo, String nome, String cognome,
                                   String email, String password, String identificativo) {
        return GestoreUtenti.getInstance().registrazione(ruolo, nome, cognome, email, password, identificativo);
    }

    /** UC2: autentica un utente tramite email istituzionale e password. */
    public UtenteDTO autenticazione(String email, String password) {
        return GestoreUtenti.getInstance().autenticazione(email, password);
    }

    /** UC8: profilo personale dello studente. */
    public UtenteDTO visualizzaProfiloPersonale(Long idStudente) {
        return GestoreUtenti.getInstance().visualizzaProfilo(idStudente);
    }

    // --- GestoreSale (UC3, UC4, UC6, UC11) ---

    /** UC3: crea una sala studio con orari, slot e aree. */
    public SalaStudioDTO creaSalaStudio(CreazioneSalaDTO richiesta) {
        return GestoreSale.getInstance().creaSalaStudio(richiesta);
    }

    /** UC4: elimina (disattiva) una sala studio, annullando le prenotazioni occupanti. */
    public void eliminaSalaStudio(Long idSalaStudio) {
        GestoreSale.getInstance().eliminaSalaStudio(idSalaStudio);
    }

    /** UC6: sale disponibili (aperte e con posti liberi) nella data indicata. */
    public List<SalaStudioDTO> consultaSaleDisponibili(LocalDate data) {
        return GestoreSale.getInstance().consultazioneSaleDisponibili(data);
    }

    /** UC6/UC7: fasce prenotabili della sala nella data, con posti liberi. */
    public List<FasciaDisponibileDTO> getFasceDisponibili(Long idSala, LocalDate data) {
        return GestoreSale.getInstance().getFasceDisponibili(idSala, data);
    }

    /** UC7: dettaglio aree/postazioni di una sala per (data, fascia). */
    public DettaglioSalaDTO selezionaDettaglioSala(Long idSala, Long idFascia, LocalDate data) {
        return GestoreSale.getInstance().selezionaDettaglioSala(idSala, idFascia, data);
    }

    /** UC11: stato in tempo reale di tutte le sale (posti liberi/attivi/confermati). */
    public List<SalaMonitoraggioDTO> monitoraSale() {
        return GestoreSale.getInstance().monitoraSale();
    }

    // --- GestorePrenotazioni (UC7, UC9, UC10, UC12, UC13, UC16) ---

    /** UC7: effettua una prenotazione (postazione specifica o assegnata automaticamente). */
    public PrenotazioneDTO effettuaPrenotazione(RichiestaPrenotazioneDTO richiesta) {
        return GestorePrenotazioni.getInstance().effettuaPrenotazione(richiesta);
    }

    /** UC9: annulla una prenotazione (entro il vincolo temporale V07). */
    public void annullaPrenotazione(Long idPrenotazione) {
        GestorePrenotazioni.getInstance().annullaPrenotazione(idPrenotazione);
    }

    /** UC10: check-in della prenotazione nel giorno e nella finestra consentita (V08). */
    public void effettuaCheckin(Long idPrenotazione) {
        GestorePrenotazioni.getInstance().effettuaCheckIn(idPrenotazione);
    }

    /** UC12: storico prenotazioni dello studente. */
    public List<PrenotazioneDTO> consultaStoricoPrenotazioni(Long idStudente) {
        return GestorePrenotazioni.getInstance().consultaStoricoPrenotazioni(idStudente);
    }

    /** UC16: transizioni automatiche di fine prenotazione (SCADUTA/CONCLUSA), invocato dallo scheduler. */
    public void gestisciTerminePrenotazioni() {
        GestorePrenotazioni.getInstance().gestisciTerminePrenotazione();
    }

    /** UC13: statistiche di servizio della giornata. */
    public StatisticheDTO monitoraStatisticheServizio() {
        return GestorePrenotazioni.getInstance().monitoraStatisticheServizio();
    }

    // --- GestoreNotifiche (UC14) ---

    /** UC14: promemoria agli studenti con prenotazione ATTIVA odierna, invocato dallo scheduler. */
    public void inviaPromemoria() {
        GestoreNotifiche.getInstance().inviaPromemoria();
    }
}
