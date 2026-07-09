package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

/**
 * Check-in effettuato (Singleton): non è più annullabile né riconfermabile;
 * a fine fascia gestisciTermine la porta in CONCLUSA.
 */
public class StatoConfermata implements StatoPrenotazione {

    private static StatoConfermata istanza;

    private StatoConfermata() {}

    public static StatoConfermata getInstance() {
        if (istanza == null) {
            istanza = new StatoConfermata();
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
        p.setStato(StatoConclusa.getInstance());
    }

    @Override
    public it.unina.prenotazioni.entity.StatoEnum getStatoEnum() {
        return it.unina.prenotazioni.entity.StatoEnum.CONFERMATA;
    }
}
