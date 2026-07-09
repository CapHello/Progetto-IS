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

    /** Tick periodico (ogni 60s, dopo 60s dall'avvio): prima le scadenze, poi i promemoria. */
    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void esegui() {
        invioNotifica();
        invioPromemoria();
    }

    /** UC16: transizioni automatiche SCADUTA/CONCLUSA con relativa notifica. */
    public void invioNotifica() {
        BibliotecaFacade.getInstance().gestisciTerminePrenotazioni();
    }

    /** UC14: promemoria delle prenotazioni ATTIVE odierne. */
    public void invioPromemoria() {
        BibliotecaFacade.getInstance().inviaPromemoria();
    }
}
