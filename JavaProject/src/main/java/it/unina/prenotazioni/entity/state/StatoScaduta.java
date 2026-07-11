package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

/** Stato terminale (Singleton): check-in non effettuato entro la tolleranza (V08), nessuna transizione ammessa. */
public class StatoScaduta implements StatoPrenotazione {

    private static StatoScaduta instance;

    private StatoScaduta() {}

    public static StatoScaduta getInstance() {
        if (instance == null) {
            instance = new StatoScaduta();
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
        return it.unina.prenotazioni.entity.StatoEnum.SCADUTA;
    }
}
