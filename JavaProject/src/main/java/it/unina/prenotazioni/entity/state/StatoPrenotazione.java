package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

public interface StatoPrenotazione {
    boolean annulla(Prenotazione prenotazione);
    boolean checkin(Prenotazione prenotazione);
    void gestisciTermine(Prenotazione prenotazione);
    it.unina.prenotazioni.entity.StatoEnum getStatoEnum();
}
