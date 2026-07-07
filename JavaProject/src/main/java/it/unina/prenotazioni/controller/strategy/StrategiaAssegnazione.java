package it.unina.prenotazioni.controller.strategy;

import it.unina.prenotazioni.entity.Postazione;
import java.util.List;

public interface StrategiaAssegnazione {
    Postazione selezionaPostazione(List<Postazione> postazioniDisponibili);
}
