package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

public class StatoScaduta implements StatoPrenotazione {

    private static StatoScaduta istanza;

    private StatoScaduta() {}

    public static StatoScaduta getInstance() {
        if (istanza == null) {
            istanza = new StatoScaduta();
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
        return it.unina.prenotazioni.entity.StatoEnum.SCADUTA;
    }
}
