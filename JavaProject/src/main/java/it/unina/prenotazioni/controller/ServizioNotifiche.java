package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.dto.UtenteDTO;
import java.util.List;

/**
 * Interfaccia del servizio di notifica, dichiarata nel controller e realizzata nel
 * boundary da AdapterServizioNotifiche (pattern Adapter). GestoreNotifiche dipende
 * solo da questa astrazione, restando isolato dal fornitore concreto del servizio.
 */
public interface ServizioNotifiche {
    void inviaNotifica(List<UtenteDTO> destinatari, String messaggio);
}
