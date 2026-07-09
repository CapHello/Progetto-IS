package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

/**
 * Pattern State: ogni stato concreto decide quali transizioni ammette.
 * I metodi ritornano false (o non fanno nulla) quando la transizione non è consentita.
 */
public interface StatoPrenotazione {
    boolean annulla(Prenotazione prenotazione);
    boolean checkin(Prenotazione prenotazione);
    void gestisciTermine(Prenotazione prenotazione);
    it.unina.prenotazioni.entity.StatoEnum getStatoEnum();
}
