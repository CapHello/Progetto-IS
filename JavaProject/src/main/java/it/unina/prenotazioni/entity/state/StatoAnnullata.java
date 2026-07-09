package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

/** Stato terminale (Singleton): prenotazione annullata, nessuna transizione ammessa. */
public class StatoAnnullata implements StatoPrenotazione {

    private static StatoAnnullata istanza;

    private StatoAnnullata() {}

    public static StatoAnnullata getInstance() {
        if (istanza == null) {
            istanza = new StatoAnnullata();
        }
        return istanza;
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
        return it.unina.prenotazioni.entity.StatoEnum.ANNULLATA;
    }
}
