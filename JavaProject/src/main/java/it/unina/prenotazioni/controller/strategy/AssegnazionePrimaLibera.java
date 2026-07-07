package it.unina.prenotazioni.controller.strategy;

import it.unina.prenotazioni.entity.Postazione;

import java.util.List;

public class AssegnazionePrimaLibera implements StrategiaAssegnazione {
    @Override
    public Postazione selezionaPostazione(List<Postazione> postazioniDisponibili) {
        /**
         * @param postazioniDisponibili lista di postazioni GIA' verificate come libere
         *        nell'istante della chiamata;
         *        (in GestorePrenotazioni ciò avviene ricalcolandola dentro il blocco synchronized).
         *        Non è thread-safe
         */
        if (postazioniDisponibili == null || postazioniDisponibili.isEmpty()) {
            return null;
        }
        // Restituisce la prima postazione libera che incontra nell'elenco
        return postazioniDisponibili.getFirst();
    }
}
