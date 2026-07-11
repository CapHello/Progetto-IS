package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * <<entity>> Singolo posto a sedere prenotabile; appartiene a un'Area e non ha
 * stato proprio: la sua disponibilità è derivata dalle prenotazioni che la impegnano.
 */
@Entity
public class Postazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    public Postazione() {}

    public Postazione(Area area) {
        this.area = area;
    }

    // Getters (niente setter: i campi li valorizzano il costruttore e JPA)
    public Long getId() { return id; }

    public Area getArea() { return area; }

    /** Sala di appartenenza, raggiunta navigando l'area. */
    public SalaStudio getSalaStudio() {
        if (this.area != null) {
            return this.area.getSalaStudio();
        }
        return null;
    }

    /**
     * Vero se la postazione è LIBERA per la (data, fascia): non esiste alcuna
     * prenotazione ATTIVA o CONFERMATA che la impegni in quello slot. La disponibilità
     * è quindi DERIVATA dallo stato delle prenotazioni.
     */
    public boolean disponibilita(LocalDate data, FasciaOraria fascia) {
        RegistroPrenotazioni registro = RegistroPrenotazioni.getInstance();
        for (Prenotazione p : registro.cercaPrenotazioniPerPostazione(id, data, fascia.getId())) {
            if (RegistroPrenotazioni.occupaSlot(p)) {
                return false;
            }
        }
        return true;
    }

}
