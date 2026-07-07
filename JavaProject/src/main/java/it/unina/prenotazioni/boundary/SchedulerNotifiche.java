package it.unina.prenotazioni.boundary;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Componente attivo (attore Tempo): a intervalli regolari gestisce i termini delle
 * prenotazioni (transizioni SCADUTA/CONCLUSA, UC16) e invia i promemoria (UC14).
 */
@Component
public class SchedulerNotifiche {

    /** Sollecitazione periodica (ogni 60s dopo un ritardo iniziale). */
    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void esegui() {
        invioNotifica();
        invioPromemoria();
    }

    public void invioNotifica() {
        BibliotecaFacade.getInstance().gestisciTerminePrenotazioni();
    }

    public void invioPromemoria() {
        BibliotecaFacade.getInstance().inviaPromemoria();
    }
}
