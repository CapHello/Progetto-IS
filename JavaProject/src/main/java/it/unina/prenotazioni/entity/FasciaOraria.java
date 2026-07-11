package it.unina.prenotazioni.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalTime;

/**
 * Intervallo orario riusabile: rappresenta sia gli slot prenotabili
 * sia gli orari lavorativi giornalieri delle sale (associazioni molti-a-molti).
 */
@Entity
public class FasciaOraria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime oraInizio;
    private LocalTime oraFine;

    public FasciaOraria() {}

    public FasciaOraria(LocalTime oraInizio, LocalTime oraFine) {
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
    }

    // Getters (niente setter: i campi li valorizzano il costruttore e JPA)
    public Long getId() { return id; }

    public LocalTime getOraInizio() { return oraInizio; }

    public LocalTime getOraFine() { return oraFine; }

    /** Etichetta "HH:mm-HH:mm": chiave testuale per confronti tra fasce (equals di String) e per la GUI. */
    public String getEtichetta() {
        return String.format("%02d:%02d-%02d:%02d",
                oraInizio.getHour(), oraInizio.getMinute(),
                oraFine.getHour(), oraFine.getMinute());
    }
}