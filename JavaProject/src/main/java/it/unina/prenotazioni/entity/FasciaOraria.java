package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

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

    public String getEtichetta() {
        return String.format("%02d:%02d-%02d:%02d",
                oraInizio.getHour(), oraInizio.getMinute(),
                oraFine.getHour(), oraFine.getMinute());
    }
}