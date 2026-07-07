package it.unina.prenotazioni.controller;

import java.time.LocalDate;
import java.util.List;

/**
 * <<Facade>> <<Singleton>>: unico punto d'accesso del sistema per le boundary.
 * Espone un'operazione per caso d'uso e smista ai quattro gestori. Le firme usano
 * solo tipi primitivi e DTO (nessun tipo entity attraversa il confine del controller).
 */
public class BibliotecaFacade {

    private static BibliotecaFacade istanza;

    private BibliotecaFacade() {}

    public static BibliotecaFacade getInstance() {
        if (istanza == null) {
            istanza = new BibliotecaFacade();
        }
        return istanza;
    }

    // --- GestoreUtenti (UC1, UC2, UC8) ---
    public Object registrazione(String ruolo, String nome, String cognome,
                                String email, String password, String identificativo) {
        return GestoreUtenti.getInstance().registrazione(ruolo, nome, cognome, email, password, identificativo);
    }

    public Object autenticazione(String email, String password) {
        return GestoreUtenti.getInstance().autenticazione(email, password);
    }

    public Object visualizzaProfiloPersonale(Long idStudente) {
        return GestoreUtenti.getInstance().visualizzaProfilo(idStudente);
    }

    // --- GestoreSale (UC3, UC4, UC6, UC11 + Aggiungi Area) ---
    public Object creaSalaStudio(String nome, String descrizione, int numeroPostazioni,
                                 String orarioApertura, String orarioChiusura, int granaMinuti,
                                 java.util.List<String> tipologie, java.util.List<Integer> postazioniAree) {
        return GestoreSale.getInstance().creaSalaStudio(nome, descrizione, numeroPostazioni,
                orarioApertura, orarioChiusura, granaMinuti, tipologie, postazioniAree);
    }

    public void eliminaSalaStudio(Long idSalaStudio) {
        GestoreSale.getInstance().eliminaSalaStudio(idSalaStudio);
    }

    public List<Object> consultaSaleDisponibili(LocalDate data) {
        return GestoreSale.getInstance().consultazioneSaleDisponibili(data);
    }

    public List<Object> getFasceDisponibili(Long idSala, LocalDate data) {
        return GestoreSale.getInstance().getFasceDisponibili(idSala, data);
    }

    public Object selezionaDettaglioSala(Long idSala, Long idFascia, LocalDate data) {
        return GestoreSale.getInstance().selezionaDettaglioSala(idSala, idFascia, data);
    }

    public List<Object> monitoraSale() {
        return GestoreSale.getInstance().monitoraSale();
    }

    // --- GestorePrenotazioni (UC5, UC7, UC9, UC10, UC12, UC13, UC16) ---
    public Object effettuaPrenotazione(Long idSala, Long idArea, Long idPostazione,
                                       LocalDate data, Long idFascia, Long idStudente) {
        return GestorePrenotazioni.getInstance()
                .effettuaPrenotazione(idSala, idArea, idPostazione, data, idFascia, idStudente);
    }

    public void annullaPrenotazione(Long idPrenotazione) {
        GestorePrenotazioni.getInstance().annullaPrenotazione(idPrenotazione);
    }

    public void effettuaCheckin(Long idPrenotazione) {
        GestorePrenotazioni.getInstance().effettuaCheckIn(idPrenotazione);
    }

    public List<Object> monitoraPrenotazioni(Long idSalaStudio) {
        return GestorePrenotazioni.getInstance().monitoraPrenotazioni(idSalaStudio);
    }

    public List<Object> consultaStoricoPrenotazioni(Long idStudente) {
        return GestorePrenotazioni.getInstance().consultaStoricoPrenotazioni(idStudente);
    }

    public void gestisciTerminePrenotazioni() {
        GestorePrenotazioni.getInstance().gestisciTerminePrenotazione();
    }

    public Object monitoraStatisticheServizio() {
        return GestorePrenotazioni.getInstance().monitoraStatisticheServizio();
    }

    // --- GestoreNotifiche (UC14) ---
    public void inviaPromemoria() {
        GestoreNotifiche.getInstance().inviaPromemoria();
    }
}
