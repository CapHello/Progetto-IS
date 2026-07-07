package it.unina.prenotazioni.controller.strategy;

import it.unina.prenotazioni.entity.Postazione;

import java.util.List;

public class AssegnazionePrimaLibera implements StrategiaAssegnazione {
    @Override
    public Postazione selezionaPostazione(List<Postazione> postazioniDisponibili) {
        if (postazioniDisponibili == null || postazioniDisponibili.isEmpty()) {
            return null;
        }
        // Restituisce la prima postazione libera che incontra nell'elenco
        return postazioniDisponibili.get(0);
    }
}
