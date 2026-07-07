package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.dto.UtenteDTO;
import it.unina.prenotazioni.entity.Observer;
import it.unina.prenotazioni.entity.Prenotazione;
import it.unina.prenotazioni.entity.RegistroPrenotazioni;
import it.unina.prenotazioni.entity.StatoEnum;
import it.unina.prenotazioni.entity.Studente;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestore (Singleton) delle notifiche. Realizza Observer (entity): a ogni cambio di
 * stato della Prenotazione riceve update() e inoltra il recapito a ServizioNotifiche.
 * Il fornitore concreto (AdapterServizioNotifiche) è iniettato dal boundary.
 */
public class GestoreNotifiche implements Observer {

    private static GestoreNotifiche istanza;

    // Iniettato dal boundary (ConfigurazioneNotifiche) → nessuna dipendenza controller→boundary.
    private ServizioNotifiche servizioNotifiche;

    private GestoreNotifiche() {}

    public static GestoreNotifiche getInstance() {
        if (istanza == null) {
            istanza = new GestoreNotifiche();
        }
        return istanza;
    }

    public void setServizioNotifiche(ServizioNotifiche servizioNotifiche) {
        this.servizioNotifiche = servizioNotifiche;
    }

    /** Observer: notifica automatica innescata da Prenotazione.setStato(...). */
    @Override
    public void update(Prenotazione prenotazione) {
        Studente studente = prenotazione.getStudente();
        if (studente == null) {
            return;
        }
        inviaNotifica(List.of(toDTO(studente)), messaggioPerStato(prenotazione));
    }

    /** Invia una notifica ai destinatari; il fallimento del recapito non è propagato (UC7/UC9 alt). */
    public void inviaNotifica(List<UtenteDTO> destinatari, String messaggio) {
        if (servizioNotifiche == null || destinatari == null || destinatari.isEmpty()) {
            return;
        }
        try {
            servizioNotifiche.inviaNotifica(destinatari, messaggio);
        } catch (RuntimeException e) {
            System.err.println("[GestoreNotifiche] recapito non riuscito: " + e.getMessage());
        }
    }

    public void inviaPromemoria(List<UtenteDTO> destinatari, String messaggio) {
        inviaNotifica(destinatari, messaggio);
    }

    /** UC14: promemoria agli studenti con prenotazione ATTIVA nella giornata corrente. */
    public void inviaPromemoria() {
        RegistroPrenotazioni registro = new RegistroPrenotazioni();
        for (Prenotazione p : registro.getPrenotazioniInScadenza()) {
            if (p.getStato().getStatoEnum() == StatoEnum.ATTIVA && LocalDate.now().equals(p.getData())) {
                Studente s = p.getStudente();
                if (s != null) {
                    inviaNotifica(List.of(toDTO(s)),
                            "Promemoria: prenotazione #" + p.getId() + " oggi nella fascia "
                                    + p.getFasciaOraria().getEtichetta());
                }
            }
        }
    }

    private String messaggioPerStato(Prenotazione p) {
        return switch (p.getStato().getStatoEnum()) {
            case ATTIVA -> "La prenotazione #" + p.getId() + " è stata registrata (stato ATTIVA).";
            case ANNULLATA -> "La prenotazione #" + p.getId() + " è stata annullata.";
            case CONFERMATA -> "Check-in registrato per la prenotazione #" + p.getId() + ".";
            case SCADUTA -> "La prenotazione #" + p.getId() + " è scaduta (check-in non effettuato).";
            case CONCLUSA -> "La prenotazione #" + p.getId() + " è conclusa.";
        };
    }

    private UtenteDTO toDTO(Studente s) {
        UtenteDTO dto = new UtenteDTO();
        dto.setId(s.getId());
        dto.setNome(s.getNome());
        dto.setCognome(s.getCognome());
        dto.setEmailIstituzionale(s.getEmailIstituzionale());
        dto.setRuolo("Studente");
        dto.setIdentificativo(s.getMatricola());
        return dto;
    }
}
