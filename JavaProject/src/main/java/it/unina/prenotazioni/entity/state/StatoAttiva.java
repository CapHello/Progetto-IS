package it.unina.prenotazioni.entity.state;

import it.unina.prenotazioni.entity.Prenotazione;

public class StatoAttiva implements StatoPrenotazione {

    private static StatoAttiva istanza;

    private StatoAttiva() {}

    public static StatoAttiva getInstance() {
        if (istanza == null) {
            istanza = new StatoAttiva();
        }
        return istanza;
    }

    @Override
    public boolean annulla(Prenotazione p) {
        p.setStato(StatoAnnullata.getInstance());
        return true;
    }

    @Override
    public boolean checkin(Prenotazione p) {
        p.setStato(StatoConfermata.getInstance());
        return true;
    }

    @Override
    public void gestisciTermine(Prenotazione p) {
        p.setStato(StatoScaduta.getInstance());
    }

    @Override
    public it.unina.prenotazioni.entity.StatoEnum getStatoEnum() {
        return it.unina.prenotazioni.entity.StatoEnum.ATTIVA;
    }
}
