package it.unina.prenotazioni.controller.strategy;

import it.unina.prenotazioni.entity.Postazione;
import java.util.List;

/**
 * Pattern Strategy (UC7): incapsula l'algoritmo di assegnazione automatica
 * della postazione, rendendolo sostituibile senza toccare GestorePrenotazioni.
 */
public interface StrategiaAssegnazione {
    Postazione selezionaPostazione(List<Postazione> postazioniDisponibili);
}
