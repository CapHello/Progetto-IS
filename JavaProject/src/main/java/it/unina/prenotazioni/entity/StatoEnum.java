package it.unina.prenotazioni.entity;

/** Stati del ciclo di vita di una Prenotazione (forma serializzabile del pattern State). */
public enum StatoEnum {
    ATTIVA,
    ANNULLATA,
    SCADUTA,
    CONFERMATA,
    CONCLUSA
}
