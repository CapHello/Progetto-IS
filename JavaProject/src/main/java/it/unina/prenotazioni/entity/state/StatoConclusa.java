package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

/** Stato terminale (Singleton): fascia terminata dopo il check-in, nessuna transizione ammessa. */
public class StatoConclusa implements StatoPrenotazione {

    private static StatoConclusa instance;

    private StatoConclusa() {}

    public static StatoConclusa getInstance() {
        if (instance == null) {
            instance = new StatoConclusa();
        }
        return instance;
    }

    @Override
    public boolean annulla(Prenotazione p) {
        return false;
    }

    @Override
    public boolean checkin(Prenotazione p) {
        return false;
    }

    @Override
    public void gestisciTermine(Prenotazione p) {
        // nulla
    }

    @Override
    public it.unina.prenotazioni.entity.StatoEnum getStatoEnum() {
        return it.unina.prenotazioni.entity.StatoEnum.CONCLUSA;
    }
}
