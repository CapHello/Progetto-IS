package it.unina.prenotazioni.boundary;

import it.unina.prenotazioni.controller.ServizioNotifiche;
import it.unina.prenotazioni.dto.UtenteDTO;
import java.util.List;

/**
 * Object Adapter: realizza l'interfaccia ServizioNotifiche (controller) traducendo le
 * chiamate verso il GatewayEmailEsterno, la cui interfaccia send(to, subject, body):int
 * è incompatibile con quella attesa dalla logica di business.
 */
public class AdapterServizioNotifiche implements ServizioNotifiche {

    private final GatewayEmailEsterno gateway = new GatewayEmailEsterno();

    /** Traduce la richiesta di notifica in una send() per ciascun destinatario; esito != 200 → eccezione. */
    @Override
    public void inviaNotifica(List<UtenteDTO> destinatari, String messaggio) {
        for (UtenteDTO dto : destinatari) {
            int esito = gateway.send(dto.getEmailIstituzionale(), "Biblioteca UniNa - Notifica", messaggio);
            if (esito != 200) {
                throw new RuntimeException(
                        "Recapito email fallito per " + dto.getEmailIstituzionale() + " (codice " + esito + ")");
            }
        }
    }
}
