package it.unina.prenotazioni.entity;

import it.unina.prenotazioni.entity.state.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Prenotazione di una postazione per una (data, fascia oraria).
 * Il ciclo di vita segue il pattern State (ATTIVA, CONFERMATA, ANNULLATA, SCADUTA, CONCLUSA);
 * come Subject del pattern Observer notifica GestoreNotifiche a ogni cambio di stato.
 */
@Entity
@Table(name = "prenotazione")
public class Prenotazione extends Subject {

    /** V08: tolleranza (in minuti) dopo l'inizio della fascia entro cui è consentito il check-in. */
    public static final int TOLLERANZA_CHECKIN_MINUTI = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;


    // Il pattern State non è persistibile: su DB si salva solo il nome dello stato,
    // mentre l'oggetto State è @Transient e viene ricostruito dai callback JPA (initStato).
    @Column(name = "stato", nullable = false)
    private String nomeStato;

    @Transient
    private StatoPrenotazione stato;

    /** Ricostruisce l'oggetto State dal nome persistito (callback JPA dopo load/persist/update). */
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

    // EAGER: la prenotazione viene usata fuori dal contesto di persistenza (entity detached),
    // quindi studente, postazione e fascia devono essere già caricati.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studente_id", nullable = false)
    private Studente studente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "postazione_id", nullable = false)
    private Postazione postazione;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fascia_oraria_id", nullable = false)
    private FasciaOraria fasciaOraria;

    // UC14: evita l'invio ripetuto del promemoria a ogni giro dello scheduler (60s).
    @Column(name = "promemoria_inviato", nullable = false)
    private boolean promemoriaInviato = false;

    public Prenotazione() { /* Costruttore vuoto richiesto da JPA. */ }

    // Getters and Setters (niente setId: lo assegna il DB al salvataggio)
    public Long getId() { return id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public StatoPrenotazione getStato() {
        if (this.stato == null && this.nomeStato != null) initStato();
        return stato;
    }

    /**
     * Transizione di stato: aggiorna il nome persistibile e notifica gli osservatori (pattern Observer).
     * @param stato stato
     */
    public void setStato(StatoPrenotazione stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato della prenotazione non può essere null");
        }
        this.stato = stato;
        this.nomeStato = stato.getStatoEnum().name();
        notifyObservers();
    }

    public boolean isPromemoriaInviato() {
        return promemoriaInviato;
    }
    public void setPromemoriaInviato(boolean promemoriaInviato) {
        this.promemoriaInviato = promemoriaInviato;
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

    /** Check-in: conferma la presenza dello studente (la transizione a CONFERMATA la decide lo stato). */
    public void effettuaCheckin() {
        boolean confermata = getStato().checkin(this);
        if (!confermata) {
            throw new IllegalStateException(
                    "Check-in non consentito nello stato attuale: " + getStato().getStatoEnum());
        }
    }

    /** Gestione automatica del termine (lo stato corrente decide se passare a SCADUTA o CONCLUSA). */
    public void gestisciTermine() {
        getStato().gestisciTermine(this);
    }

    /**
     * Annullo forzato usato in EliminaSalaStudio (UC4): imposta ANNULLATA da qualsiasi
     * stato, senza il vincolo temporale dell'annullamento volontario. Scrive i campi
     * direttamente (non passa da setStato) perché la notifica cumulativa la invia GestoreSale.
     */
    public void criticalAnnullaPrenotazione() {
        this.stato = StatoAnnullata.getInstance();
        this.nomeStato = this.stato.getStatoEnum().name();
    }

    /**
     * UC10: il check-in richiede prenotazione ATTIVA, riferita alla giornata corrente ed
     * entro la finestra consentita (dalla mezzanotte fino a inizio fascia + tolleranza, V08).
     * Il limite superiore è verificato qui in modo sincrono, senza dipendere dal tempismo
     * dello scheduler che marca le prenotazioni SCADUTE.
     */
    public void verificaPrenotazioneAttivaInDataCorrente() {
        if (getStato() == null || getStato().getStatoEnum() != StatoEnum.ATTIVA) {
            throw new IllegalStateException("La prenotazione non è in stato ATTIVA");
        }
        if (!LocalDate.now(ZoneId.of("Europe/Rome")).equals(data)) {
            throw new IllegalStateException("Il check-in è consentito solo nel giorno della prenotazione");
        }
        LocalDateTime limite = LocalDateTime.of(data, fasciaOraria.getOraInizio())
                .plusMinutes(TOLLERANZA_CHECKIN_MINUTI);
        if (LocalDateTime.now(ZoneId.of("Europe/Rome")).isAfter(limite)) {
            throw new IllegalStateException("superata la tolleranza di " + TOLLERANZA_CHECKIN_MINUTI
                    + " minuti dall'inizio della fascia oraria");
        }
    }

    // V07: l'annullamento è consentito solo fino a 6 ore prima dell'inizio della fascia.
    private void verificaIntervalloAnnullamentoPrenotazione() {
        if (data == null || fasciaOraria == null) {
            throw new IllegalStateException("Prenotazione priva di data o fascia oraria");
        }
        LocalDateTime inizio = LocalDateTime.of(data, fasciaOraria.getOraInizio());
        if (LocalDateTime.now(ZoneId.of("Europe/Rome")).isAfter(inizio.minusHours(6))) {
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
