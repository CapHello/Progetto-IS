package it.unina.prenotazioni.entity;

import it.unina.prenotazioni.entity.state.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prenotazione")
public class Prenotazione extends Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;


    // Per garantire la compatibilità tra il Pattern State utilizzato
    // e la persistenza JPA è stato introdotta la string nomeStato.
    @Column(name = "stato", nullable = false)
    private String nomeStato;

    @Transient
    private StatoPrenotazione stato;

    @PostLoad
    @PostPersist
    @PostUpdate
    private void initStato() {
        if ("ATTIVA".equals(nomeStato)) this.stato = StatoAttiva.getInstance();
        else if ("ANNULLATA".equals(nomeStato)) this.stato = StatoAnnullata.getInstance();
        else if ("SCADUTA".equals(nomeStato)) this.stato = StatoScaduta.getInstance();
        else if ("CONFERMATA".equals(nomeStato)) this.stato = StatoConfermata.getInstance();
        else if ("CONCLUSA".equals(nomeStato)) this.stato = StatoConclusa.getInstance();
    }

    // FetchType.EAGER indica una strategia di caricamento dei dati immediata.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studente_id", nullable = false)
    private Studente studente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "postazione_id", nullable = false)
    private Postazione postazione;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fascia_oraria_id", nullable = false)
    private FasciaOraria fasciaOraria;

    public Prenotazione() { /* Imposto vuoto dalla specifica JPA. */ }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public StatoPrenotazione getStato() {
        if (this.stato == null && this.nomeStato != null) initStato();
        return stato;
    }

    // setStato incapsula la transizione: aggiorna la rappresentazione serializzabile e notifica gli osservatori (pattern Observer).
    public void setStato(StatoPrenotazione stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato della prenotazione non può essere null");
        }
        this.stato = stato;
        this.nomeStato = stato.getStatoEnum().name();
        notifyObservers();
    }

    // --- Azioni del pattern State (delegano allo stato corrente) ---

    /** Annulla la prenotazione previa verifica del vincolo temporale (V07, 6 ore). */
    public void annullaPrenotazione() {
        verificaIntervalloAnnullamentoPrenotazione();
        boolean annullata = getStato().annulla(this);
        if (!annullata) {
            throw new IllegalStateException(
                    "La prenotazione non può essere annullata nello stato attuale: " + getStato().getStatoEnum());
        }
    }

    /** Check-in: conferma la presenza dello studente (delega allo stato → CONFERMATA). */
    public void effettuaCheckin() {
        boolean confermata = getStato().checkin(this);
        if (!confermata) {
            throw new IllegalStateException(
                    "Check-in non consentito nello stato attuale: " + getStato().getStatoEnum());
        }
    }

    /** Gestione automatica del termine (delega allo stato → SCADUTA / CONCLUSA). */
    public void gestisciTermine() {
        getStato().gestisciTermine(this);
    }

    /**
     * Annullo forzato usato in EliminaSalaStudio (UC4): imposta ANNULLATA da qualsiasi
     * stato, senza applicare il vincolo temporale dell'annullamento volontario.
     */
    public void criticalAnnullaPrenotazione() {
        this.stato = StatoAnnullata.getInstance();
        this.nomeStato = this.stato.getStatoEnum().name();
    }

    /** UC10: il check-in richiede prenotazione ATTIVA e riferita alla giornata corrente. */
    public void verificaPrenotazioneAttivaInDataCorrente() {
        if (getStato() == null || getStato().getStatoEnum() != StatoEnum.ATTIVA) {
            throw new IllegalStateException("La prenotazione non è in stato ATTIVA");
        }
        if (!LocalDate.now().equals(data)) {
            throw new IllegalStateException("Il check-in è consentito solo nel giorno della prenotazione");
        }
    }

    // V07: l'annullamento è consentito solo fino a 6 ore prima dell'inizio della fascia.
    private void verificaIntervalloAnnullamentoPrenotazione() {
        if (data == null || fasciaOraria == null) {
            throw new IllegalStateException("Prenotazione priva di data o fascia oraria");
        }
        LocalDateTime inizio = LocalDateTime.of(data, fasciaOraria.getOraInizio());
        if (LocalDateTime.now().isAfter(inizio.minusHours(6))) {
            throw new IllegalStateException(
                    "Annullamento non consentito: mancano meno di 6 ore all'inizio della fascia oraria");
        }
    }

    public Studente getStudente() { return studente; }

    public void setStudente(Studente studente) {
        this.studente = studente;
    }

    public Postazione getPostazione() { return postazione; }
    public void setPostazione(Postazione postazione) { this.postazione = postazione; }

    public FasciaOraria getFasciaOraria() { return fasciaOraria; }
    public void setFasciaOraria(FasciaOraria fasciaOraria) { this.fasciaOraria = fasciaOraria; }

    // --- Pattern Observer (Subject) ---
    @Override
    public void notifyObservers() {
        for (Observer obs : observers) {
            obs.update(this);
        }
    }
}
