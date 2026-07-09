package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * <<entity>> Intervallo orario riusabile: rappresenta sia gli slot prenotabili
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalTime getOraInizio() { return oraInizio; }
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }

    public LocalTime getOraFine() { return oraFine; }
    public void setOraFine(LocalTime oraFine) { this.oraFine = oraFine; }

    /** Etichetta "HH:mm-HH:mm": chiave testuale per confronti tra fasce (equals di String) e per la GUI. */
    public String getEtichetta() {
        return String.format("%02d:%02d-%02d:%02d",
                oraInizio.getHour(), oraInizio.getMinute(),
                oraFine.getHour(), oraFine.getMinute());
    }
}