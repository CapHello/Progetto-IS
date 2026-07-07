package it.unina.prenotazioni.boundary;

/**
 * <<external>> Gateway del servizio di messaggistica esterno (V01).
 *
 * STUB volutamente non implementato: per requisito di progetto l'integrazione reale con
 * il gateway esterno NON va sviluppata. Simula l'invio stampando a console e restituendo
 * un esito HTTP-like (200 = OK).
 */
public class GatewayEmailEsterno {

    public int send(String to, String subject, String body) {
        System.out.println("[GatewayEmailEsterno-STUB] to=" + to + " | subject=" + subject + " | body=" + body);
        return 200; // HTTP OK simulato
    }
}
