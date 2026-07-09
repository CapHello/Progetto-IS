package it.unina.prenotazioni.entity;

/**
 * Observer del pattern omonimo. L'interfaccia vive nel layer entity così che
 * Prenotazione notifichi i cambi di stato senza conoscere chi la osserva
 * (GestoreNotifiche, nel controller, la implementa: la dipendenza non risale mai).
 */
public interface Observer {
    void update(Prenotazione prenotazione);
}
