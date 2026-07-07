package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

public class StatoConclusa implements StatoPrenotazione {

    private static StatoConclusa istanza;

    private StatoConclusa() {}

    public static StatoConclusa getInstance() {
        if (istanza == null) {
            istanza = new StatoConclusa();
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
        return it.unina.prenotazioni.entity.StatoEnum.CONCLUSA;
    }
}
