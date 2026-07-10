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
 * <<control>> Gestore (Singleton) delle notifiche. Realizza Observer (entity): a ogni
 * cambio di stato della Prenotazione riceve update() e inoltra il recapito a
 * ServizioNotifiche. Il fornitore concreto (AdapterServizioNotifiche) è iniettato dal boundary.
 */
public class GestoreNotifiche implements Observer {

    private static GestoreNotifiche istanza;
    private final RegistroPrenotazioni registroPrenotazioni = RegistroPrenotazioni.getInstance();
    // Iniettato dal boundary (ConfigurazioneNotifiche) → nessuna dipendenza controller→boundary.
    private ServizioNotifiche servizioNotifiche;

    private GestoreNotifiche() {}

    public static GestoreNotifiche getInstance() {
        if (istanza == null) {
            istanza = new GestoreNotifiche();
        }
        return istanza;
    }

    /** Iniettato all'avvio da ConfigurazioneNotifiche (boundary): il controller conosce solo l'interfaccia. */
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
        inviaNotifica(List.of(toUtenteDTO(studente)), messaggioPerStato(prenotazione));
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

    /**
     * UC14: promemoria agli studenti con prenotazione ATTIVA nella giornata corrente.
     * Il flag persistito promemoriaInviato garantisce un solo invio per prenotazione,
     * anche se lo scheduler richiama il metodo ogni 60 secondi.
     */
    public void inviaPromemoria() {
        for (Prenotazione p : registroPrenotazioni.getPrenotazioniInScadenza()) {
            if (p.getStato().getStatoEnum() == StatoEnum.ATTIVA && LocalDate.now().equals(p.getData()) && !p.isPromemoriaInviato()) {
                Studente s = p.getStudente();
                if (s != null) {
                    inviaNotifica(List.of(toUtenteDTO(s)),
                            "Promemoria: prenotazione #" + p.getId() + " oggi nella fascia "
                                    + p.getFasciaOraria().getEtichetta());
                    p.setPromemoriaInviato(true);
                    registroPrenotazioni.aggiorna(p);
                }
            }
        }
    }

    /** Testo della notifica in funzione dello stato raggiunto dalla prenotazione. */
    private String messaggioPerStato(Prenotazione p) {
        return switch (p.getStato().getStatoEnum()) {
            case ATTIVA -> "La prenotazione #" + p.getId() + " è stata registrata (stato ATTIVA).";
            case ANNULLATA -> "La prenotazione #" + p.getId() + " è stata annullata.";
            case CONFERMATA -> "Check-in registrato per la prenotazione #" + p.getId() + ".";
            case SCADUTA -> "La prenotazione #" + p.getId() + " è scaduta (check-in non effettuato).";
            case CONCLUSA -> "La prenotazione #" + p.getId() + " è conclusa.";
        };
    }

    /**
     * Converte lo studente nel DTO destinatario delle notifiche. Unico punto di
     * conversione Studente→UtenteDTO del layer controller (visibilità di package):
     * lo usano anche GestoreSale (UC4) e GestorePrenotazioni (UC9).
     */
    UtenteDTO toUtenteDTO(Studente s) {
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
