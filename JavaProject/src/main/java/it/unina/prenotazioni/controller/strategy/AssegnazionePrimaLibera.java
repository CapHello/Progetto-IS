package it.unina.prenotazioni.controller.strategy;

import it.unina.prenotazioni.entity.Postazione;

import java.util.List;

/** Strategia concreta: assegna la prima postazione della lista dei posti liberi. */
public class AssegnazionePrimaLibera implements StrategiaAssegnazione {

    /**
     * La lista ricevuta è già verificata come libera al momento della chiamata
     * (GestorePrenotazioni la ricalcola dentro il blocco synchronized di UC7).
     */
    @Override
    public Postazione selezionaPostazione(List<Postazione> postazioniDisponibili) {
        if (postazioniDisponibili == null || postazioniDisponibili.isEmpty()) {
            return null;
        }
        return postazioniDisponibili.getFirst();
    }
}
