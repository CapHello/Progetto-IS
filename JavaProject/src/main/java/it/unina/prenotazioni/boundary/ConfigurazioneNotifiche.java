package it.unina.prenotazioni.boundary;

import it.unina.prenotazioni.controller.GestoreNotifiche;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Cablaggio (lato boundary) del servizio di notifica concreto nel GestoreNotifiche.
 * Mantiene le dipendenze nel verso corretto del BCED: il boundary conosce il controller,
 * mai il contrario; il controller resta legato alla sola interfaccia ServizioNotifiche.
 */
@Component
public class ConfigurazioneNotifiche {

    @PostConstruct
    public void configura() {
        GestoreNotifiche.getInstance().setServizioNotifiche(new AdapterServizioNotifiche());
    }
}
