package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <<entity>> Radice dell'aggregato Sala Studio (Aree, Postazioni, Fasce orarie).
 * L'eliminazione (UC4) è un soft delete tramite il flag {@code attiva}, per
 * preservare lo storico delle prenotazioni.
 */
@Entity
public class SalaStudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descrizione;
    private int numeroPostazioniTotali;

    @Column(name = "attiva", nullable = false)
    private boolean attiva = true;

    @OneToMany(mappedBy = "salaStudio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Area> aree = new ArrayList<>();

    // Slot prenotabili (unione di tutti i giorni). EAGER: le sale sono usate detached
    // in UC6/UC7; il cascade PERSIST salva le fasce insieme alla sala in un'unica transazione.
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "sala_slot_prenotabili",
            joinColumns = @JoinColumn(name = "sala_id"),
            inverseJoinColumns = @JoinColumn(name = "fascia_id")
    )
    private List<FasciaOraria> slotOrario = new ArrayList<>();

    // Orario di apertura/chiusura per giorno. @OrderColumn rende la lista indicizzata:
    // l'ordine di inserimento è garantito anche dopo il reload (0 = Lunedì … 4 = Venerdì).
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "sala_orari_lavorativi",
            joinColumns = @JoinColumn(name = "sala_id"),
            inverseJoinColumns = @JoinColumn(name = "fascia_id")
    )
    @OrderColumn(name = "giorno_settimana")
    private List<FasciaOraria> orarioLavorativo = new ArrayList<>();

    public SalaStudio() {}

    public SalaStudio(String nome, String descrizione, int numeroPostazioniTotali) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.numeroPostazioniTotali = numeroPostazioniTotali;
    }

    // Getters and Setters (niente setter per id e numeroPostazioniTotali: li assegnano DB e costruttore)
    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public int getNumeroPostazioniTotali() { return numeroPostazioniTotali; }

    public boolean isAttiva() { return attiva; }
    public void setAttiva(boolean attiva) { this.attiva = attiva; }

    // --- Costruzione in memoria (usata in CreaSalaStudio, prima della persistenza) ---

    /** Aggiunge una fascia oraria (slot prenotabile) all'orario della sala. */
    public void addFascia(FasciaOraria fascia) {
        slotOrario.add(fascia);
    }

    /** Aggiunge l'orario lavorativo del giorno successivo (max 5, Lunedì-Venerdì). */
    public void addOrarioLavorativo(FasciaOraria fascia) {
        if (this.orarioLavorativo.size() >= 5) {
            throw new IllegalStateException("Impossibile aggiungere un altro orario: limite massimo di 5 orari lavorativi raggiunto per questa sala.");
        }
        this.orarioLavorativo.add(fascia);
    }

    /**
     * Aggiunge (in memoria) una nuova Area con le sue postazioni; la persistenza avviene
     * a cascata col salvataggio della sala (UC3). Verifica V04 (almeno una postazione)
     * e che il totale assegnato non superi la capienza della sala.
     */
    public Area aggiungiArea(String tipologia, int numPostazioni) {
        if (numPostazioni < 1) {
            throw new IllegalArgumentException("Ogni area deve contenere almeno una postazione (V04)");
        }

        // Calcoliamo quante postazioni sono già state assegnate alle altre aree
        int postazioniAssegnate = aree.stream()
                .mapToInt(a -> a.getPostazioni().size())
                .sum();

        verificaNumeroPostazioni(postazioniAssegnate + numPostazioni);

        Area area = new Area(tipologia, this);

        area.creaPostazioni(numPostazioni);

        aree.add(area);

        return area;
    }

    /**
     * Crea l'area di default "comune" (V19) con le postazioni rimanenti non assegnate ad
     * aree specifiche (step 6 dello scenario CreaSalaStudio).
     */
    public void creaAreaDefault(int postazioniRimanenti) {
        aggiungiArea("comune", postazioniRimanenti);
    }

    // Il numero di postazioni assegnate alle aree non può superare il totale della sala.
    private void verificaNumeroPostazioni(int numAssegnate) {
        if (numAssegnate > numeroPostazioniTotali) {
            throw new IllegalArgumentException(
                    "Le postazioni assegnate alle aree superano il totale della sala studio");
        }
    }

    // --- Consultazione / disponibilità ---

    /** V06: la sala è aperta nei giorni feriali (lunedì-venerdì). */
    public boolean verificaDataInGiorniApertura(LocalDate data) {
        DayOfWeek giorno = data.getDayOfWeek();
        return !giorno.equals(DayOfWeek.SATURDAY) && !giorno.equals(DayOfWeek.SUNDAY);
    }

    /**
     * Recupera l'orario lavorativo (apertura e chiusura) per una specifica data.
     * Sfrutta l'indice della lista per mappare i giorni feriali (Lunedì = 0, Venerdì = 4).
     */
    private FasciaOraria getOrarioLavorativoPerData(LocalDate data) {
        if (!verificaDataInGiorniApertura(data)) {
            return null; // Chiuso nel weekend
        }

        int indiceGiorno = data.getDayOfWeek().getValue() - 1;

        if (orarioLavorativo != null && indiceGiorno < orarioLavorativo.size()) {
            return orarioLavorativo.get(indiceGiorno);
        }
        return null;
    }

    /**
     * Fasce prenotabili per la data indicata (V06): filtra gli slot della sala
     * tenendo solo quelli interni all'orario lavorativo del giorno specifico.
     */
    public List<FasciaOraria> getFasceOrariePrestabilite(LocalDate data) {
        FasciaOraria orarioDelGiorno = getOrarioLavorativoPerData(data);

        if (orarioDelGiorno == null) {
            return new ArrayList<>();
        }

        List<FasciaOraria> slotDisponibiliOggi = new ArrayList<>();

        for (FasciaOraria slot : slotOrario) {
            boolean iniziaDopoApertura = !slot.getOraInizio().isBefore(orarioDelGiorno.getOraInizio());
            boolean finiscePrimaChiusura = !slot.getOraFine().isAfter(orarioDelGiorno.getOraFine());

            if (iniziaDopoApertura && finiscePrimaChiusura) {
                slotDisponibiliOggi.add(slot);
            }
        }

        return slotDisponibiliOggi;
    }

    /** True se esiste almeno una postazione libera nella sala per (data, fascia). */
    public boolean verificaDisponibilita(LocalDate data, FasciaOraria fascia) {
        RegistroSale registro = RegistroSale.getInstance();
        for (Area area : registro.getAreePerSala(id)) {
            if (!area.getPostazioniDisponibili(data, fascia).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
